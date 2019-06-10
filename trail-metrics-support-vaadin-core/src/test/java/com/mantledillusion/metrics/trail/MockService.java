package com.mantledillusion.metrics.trail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MockService {

	public static class MockSession extends HashMap<Class<?>, Object> {
		
		private static final long serialVersionUID = 1L;
		
		private final String id;
		private final List<MockRequest> requests = new ArrayList<>();

		public MockSession(String id) {
			this.id = id;
		}

		public String getId() {
			return id;
		}
		
		public void addRequest(MockRequest request) {
			this.requests.add(request);
		}
		
		public MockRequest getCurrentRequest() {
			return this.requests.get(this.requests.size()-1);
		}
	}
	
	public static class MockRequest extends HashMap<String, Object> {
		
		private static final long serialVersionUID = 1L;
		
	}
}
