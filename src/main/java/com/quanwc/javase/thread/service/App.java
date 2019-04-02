package com.quanwc.javase.thread.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) {
		new App().test();
	}
	
	public void test() {
		ExecutorService executorService = Executors.newFixedThreadPool(70);
		for (int i = 0; i < 70000000; i = i + 10000) {
			System.out.println(i);
			executorService.execute(new Deal(i, i + 10000));
		}
		executorService.shutdown();
	}
	
	class Deal implements Runnable {
		private Integer skip;
		private  Integer limit;
		public Deal(Integer skip, Integer limit ) {
			this.skip = skip;
			this.limit = limit;
		}
		
		@Override
		public void run() {
			for(int i = skip; i < limit; i++) {
				System.out.println(Thread.currentThread() + ":" + i);
			}
		}
	}


	
}
