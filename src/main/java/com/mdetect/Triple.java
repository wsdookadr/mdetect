package com.mdetect;

/*
 * Triple class (holds three objects)
 */
class Triple<A, B, C> extends Object {
	public A a;
	public B b;
	public C c;

	public Triple(A a, B b, C c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}

	public void set1(A a) {
		this.a = a;
	}

	public void set2(B b) {
		this.b = b;
	}

	public void set3(C c) {
		this.c = c;
	}

	public A get1() {
		return a;
	}

	public B get2() {
		return b;
	}

	public C get3() {
		return c;
	}

	public boolean equals(Triple<A, B, C> o) {
		return o.get1().equals(get1()) && o.get2().equals(get2()) && o.get3().equals(get3());
	}
}