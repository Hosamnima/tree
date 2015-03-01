
package code.analysis.test;


public class MyNewClass99
    extends MyNewClass98
{


    public void m() {
        super.m();
    }

    public static void main(String[] args) {
        MyNewClass99 myClass;
        myClass = new MyNewClass99();
        myClass.m();
    }

}
