package test;

import plantUMLGenerator.PlantUMLGenerator;

public class Main {
	public static void main(String[] args) {
		
		Object[] objs = new Object[2];
		objs[0] = new A();
		objs[1] = new B();
		
		Object[] objs2 = new Object[2];
		C c = new C();
		D d = new D();
		
		c.setD(d);
		objs2[0] = c;
		objs2[1] = d;
		
		
		System.out.println(PlantUMLGenerator.generate(objs));
		
		System.out.println("******* end *********");
		
		System.out.println(PlantUMLGenerator.generate(objs2));
		
		
//		C c = new C();
//		C c2 = new C();
//		D d = new D();
//		
//		c.setD(d);
//		d.setC(c2);
	}
}
