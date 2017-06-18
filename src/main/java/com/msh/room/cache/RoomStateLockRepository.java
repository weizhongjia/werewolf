package com.msh.room.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by zhangruiqian on 2017/6/18.
 */
//TODO 后续使用redis进行锁记录
public class RoomStateLockRepository {
    private Map<String, RoomLock> lockCache = new ConcurrentHashMap<>();

    public synchronized RoomLock getLock(String roomCode) {
        RoomLock roomLock = lockCache.get(roomCode);
        if (roomLock == null) {
            roomLock = new RoomLock();
            lockCache.put(roomCode, roomLock);
        }
        return roomLock;
    }

    class RoomLock {
    }

    static class CounterRunnable implements Runnable {
        RoomStateLockRepository roomStateLock;
        Integer[] data;
        int number;
        String name;

        public CounterRunnable(RoomStateLockRepository roomStateLock, Integer[] data, int number, String name) {
            this.roomStateLock = roomStateLock;
            this.data = data;
            this.number = number;
            this.name = name;
        }

        @Override
        public void run() {
            synchronized (roomStateLock.getLock(name)) {
                System.out.println("####" + number + " start ####");
                for (int i = 0; i < 5; i++) {
                    Integer integer = data[0];
                    integer++;
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    data[0] = integer;
//                    throw new RuntimeException("error" + number);
                    System.out.println("[" + number + "]:" + data[0]);
                }
                System.out.println("####" + number + " end ####");
            }
        }
    }

    public static void main(String[] args) {
        RoomStateLockRepository roomStateLock = new RoomStateLockRepository();
        String name = "abc";
        final Integer[] number = {0};

        Thread thread1 = new Thread(new CounterRunnable(roomStateLock, number, 1, name));
        Thread thread2 = new Thread(new CounterRunnable(roomStateLock, number, 2, name));
        Thread thread3 = new Thread(new CounterRunnable(roomStateLock, number, 3, name));
        Thread thread4 = new Thread(new CounterRunnable(roomStateLock, number, 4, name));
        Thread thread5 = new Thread(new CounterRunnable(roomStateLock, number, 5, name));
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();
    }
}


