package com.cryo.cache;

import java.io.IOException;

public final class Cache {

	public static Store STORE;

	public static boolean IS_LOADED;
	public static boolean CACHE_ERROR;

	public static String LOCATION;

	private Cache() {

	}

	public static void main(String[] args) {
//		init("");
	}
	
	public static void init(String path) throws IOException {
		STORE = new Store(path);
		LOCATION = path;
		IS_LOADED = true;
	}
}
