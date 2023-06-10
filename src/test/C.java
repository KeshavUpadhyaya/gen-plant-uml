package test;

public class C {
	
	private D d;

	public D getD() {
		return d;
	}

    public void setD(D d) {
        if (this.d != d) {
            D previousD = this.d;
            this.d = d;
            if (previousD != null) {
                previousD.setC(null); // remove the previous relationship
            }
            if (d != null && d.getC() != this) {
                d.setC(this);
            }
        }
    }
	
	

}
