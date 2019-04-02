package com.quanwc.javase.thread.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class App2 {
	public static void main(String[] args) {
		new App2().test();
	}

	public void test() {
		ExecutorService executorService = Executors.newFixedThreadPool(70);
		for (int i = 0; i < 70000000; i = i + 10000) {
			int start = i;
			executorService.execute(() -> deal(start, start + 1000));
		}
		executorService.shutdown();
	}

	public void deal(Integer skip, Integer limit) {
		for (int i = skip; i < limit; i++) {
			System.out.println(Thread.currentThread() + ":" + i);
		}
	}

}
