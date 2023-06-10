package test;

public class D {

	private C c;

	public C getC() {
		return c;
	}

    public void setC(C c) {
        if (this.c != c) {
            C previousC = this.c;
            this.c = c;
            if (previousC != null) {
                previousC.setD(null); // remove the previous relationship
            }
            if (c != null && c.getD() != this) {
                c.setD(this);
            }
        }
    }
	
	
}
