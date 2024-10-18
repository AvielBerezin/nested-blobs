package blobs.server.network;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

public class KeyedExecutor<Key> {
    private static final AtomicInteger counter = new AtomicInteger(0);
    private final Object syncObject;
    private final AtomicBoolean stopped;
    private ConstSizeNode<Key> min;
    private final Map<Key, SyncTasks<Key>> selector;

    public KeyedExecutor(int numberOfThreads) {
        int uniqueKeyedExecutorId = counter.getAndIncrement();
        syncObject = new Object();
        min = new ConstSizeNode<>(this, 0);
        stopped = new AtomicBoolean(false);
        selector = new ConcurrentHashMap<>();
        for (int i = 0; i < numberOfThreads; i++) {
            SyncTasks<Key> syncTasks = new SyncTasks<>(min, stopped, syncObject, selector);
            Thread thread = new Thread(() -> {
                while (true) {
                    try {
                        if (stopped.get()) {
                            syncTasks.queue.clear();
                            return;
                        }
                        syncTasks.executeNext();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            thread.setName("KeyedExecutor[" + uniqueKeyedExecutorId + "-" + i + "]");
            thread.start();
            min.entries.add(syncTasks);
        }
    }

    public void execute(Key key, Runnable task) {
        synchronized (syncObject) {
            SyncTasks<Key> syncTasks = selector.get(key);
            if (syncTasks == null) {
                syncTasks = min.entries.iterator().next();
                selector.put(key, syncTasks);
            }
            syncTasks.add(key, task);
        }
    }

    public void stop() {
        stopped.set(true);
    }

    private record KeyedTask<Key>(Key key, Runnable task) {
    }

    private static class SyncTasks<Key> {
        private ConstSizeNode<Key> node;
        private final AtomicBoolean stopped;
        private final Map<Key, SyncTasks<Key>> selector;
        private int size;
        private final BlockingQueue<KeyedTask<Key>> queue;
        private final Map<Key, Integer> keyedSizes;
        private final Object syncObject;

        private SyncTasks(ConstSizeNode<Key> node, AtomicBoolean stopped, Object syncObject, Map<Key, SyncTasks<Key>> selector) {
            this.node = node;
            this.stopped = stopped;
            this.syncObject = syncObject;
            this.selector = selector;
            queue = new LinkedBlockingQueue<>();
            size = 0;
            keyedSizes = new ConcurrentHashMap<>();
        }

        private void add(Key key, Runnable task) {
            if (stopped.get()) {
                return;
            }
            queue.add(new KeyedTask<>(key, task));
            increment(key);
        }

        private void executeNext() throws InterruptedException {
            KeyedTask<Key> keyedTask;
            do {
                keyedTask = queue.poll(1, TimeUnit.SECONDS);
                if (stopped.get()) {
                    return;
                }
            } while (keyedTask == null);
            try {
                try {
                    keyedTask.task.run();
                } finally {
                    synchronized (syncObject) {
                        decrement(keyedTask.key);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        private void decrement(Key key) {
            size--;
            int size = keyedSizes.get(key);
            if (size > 1) {
                keyedSizes.put(key, size - 1);
            } else {
                keyedSizes.remove(key);
                selector.remove(key);
            }
            updateNodeWithSizeChange(n -> n.prev, ConstSizeNode::pushPrev);
        }

        public void increment(Key key) {
            keyedSizes.putIfAbsent(key, 0);
            keyedSizes.put(key, keyedSizes.get(key) + 1);
            size++;
            updateNodeWithSizeChange(n -> n.next, ConstSizeNode::pushNext);
        }

        private void updateNodeWithSizeChange(Function<ConstSizeNode<Key>, ConstSizeNode<Key>> advance,
                                              BiFunction<ConstSizeNode<Key>, SyncTasks<Key>, ConstSizeNode<Key>> pushAdvanced) {
            node.entries.remove(this);
            if (advance.apply(node) != null && advance.apply(node).size == size) {
                advance.apply(node).entries.add(this);
            } else {
                pushAdvanced.apply(node, this);
            }
            if (node.entries.isEmpty()) {
                node.unlink();
            }
            node = advance.apply(node);
        }
    }

    private static class ConstSizeNode<Key> {
        private final KeyedExecutor<Key> executor;
        private final int size;
        private final Set<SyncTasks<Key>> entries;
        private ConstSizeNode<Key> prev;
        private ConstSizeNode<Key> next;

        public ConstSizeNode(KeyedExecutor<Key> executor, int size) {
            this.size = size;
            entries = new HashSet<>();
            this.executor = executor;
        }

        private void unlink() {
            if (prev == null) {
                executor.min = next;
            }
            if (prev != null) {
                prev.next = next;
            }
            if (next != null) {
                next.prev = prev;
            }
        }

        private ConstSizeNode<Key> pushNext(SyncTasks<Key> entry) {
            ConstSizeNode<Key> node = new ConstSizeNode<>(executor, entry.size);
            node.entries.add(entry);
            node.prev = this;
            if (next != null) {
                next.prev = node;
                node.next = next;
            }
            next = node;
            return node;
        }

        private ConstSizeNode<Key> pushPrev(SyncTasks<Key> entry) {
            boolean wasMin = prev == null;
            ConstSizeNode<Key> node = new ConstSizeNode<>(executor, entry.size);
            node.entries.add(entry);
            node.next = this;
            if (prev != null) {
                prev.next = node;
                node.prev = prev;
            }
            prev = node;
            if (wasMin) {
                executor.min = prev;
            }
            return node;
        }
    }
}
