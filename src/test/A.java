package test;

public class A extends B implements I{
	
	@Override
	public int add(int y) {
		return x + y;
	}
	
	private B b;

	public B getB() {
		return b;
	}

	public void setB(B b) {
		this.b = b;
	}
	
	
}
