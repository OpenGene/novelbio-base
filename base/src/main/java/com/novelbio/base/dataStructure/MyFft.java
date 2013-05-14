package com.novelbio.base.dataStructure;

public class MyFft {

	complex b[];

	/*
	 * 
	 * main
	 */
	public static void main(String arg[]) {
		MyFft mf = new MyFft();
		final int n = 16;

		int dd[] = new int[n];
		mf.b = new complex[n];
		for (int c = 0; c < n; c++) { // 轮流赋值
			mf.b[c] = new complex(c, 0);
			// System.out.print(mf.b[c].r+"+j"+mf.b[c].i+"\n");
		}
		System.out.print("\n\n");

		mf.b = mf.changedLow(mf.b, n);
		mf.b = mf.fft_2(mf.b, n, -1);
		// 输出
		for (int c = 0; c < n; c++) {
			System.out.print(mf.b[c].r + "+j" + mf.b[c].i + "\n");
		}
	}

	public MyFft() {

	}

	/*
	 * 乘积因子
	 */
	public complex complex_exp(complex z) {
		complex r = new complex();
		double expx = Math.exp(z.r);
		r.r = expx * Math.cos(z.i);
		r.i = expx * Math.sin(z.i);
		return r;
	}

	/*
	 * 基-2 fft蝶形变换fft_tepy=1正变换, -1反变换
	 */
	public complex[] fft_2(complex[] a, int length, int fft_tepy) {

		double pisign = fft_tepy * Math.PI;
		// System.out.print(" pisign:"+pisign+"\n");
		complex t = new complex();
		int l = 1;

		while (l < length) {
			for (int m = 0; m < l; ++m) {
				int temp_int = l * 2; // 左移相当于,l乘以2
				for (int i = m; temp_int < 0 ? i >= (length - 1) : i < length; i += temp_int) {
					complex temp = new complex(0.0, m * pisign / l);

					complex temp_exp = complex_exp(temp);
					t.r = a[i + l].r * temp_exp.r - a[i + l].i * temp_exp.i;
					t.i = a[i + l].r * temp_exp.i + a[i + l].i * temp_exp.r;

					a[i + l].r = a[i].r - t.r;
					a[i + l].i = a[i].i - t.i;
					a[i].r = a[i].r + t.r;
					a[i].i = a[i].i + t.i;

				} // end for i

			} // end for m
			System.out.print("\n now is the loop and l=" + l + "\n");
			for (int c = 0; c < length; c++) {
				System.out.print(a[c].r + "+j" + a[c].i + "\n");
			}

			l = l * 2;
		}// end while
			// 左移相当于,l乘以2
		return a;
	}

	/*
	 * 实现倒码
	 */
	public static complex[] changedLow(complex[] a, int length) {
		int mr = 0;

		for (int m = 1; m < length; ++m) {
			int l = length / 2;
			while (mr + l >= length) {
				l = l >> 1; // 右移相当于,l除以2
			}
			mr = mr % l + l;
			if (mr > m) {
				complex t = new complex();
				t = a[m];
				a[m] = a[mr];
				a[mr] = t;
			}
		}

		return a;
	}
}
/*
*复数类
**/
class complex{
double r,i;
   public complex(){
   
   }
   public complex(double r,double i){
    this.r=r; //实部
    this.i=i; //虚部
   }
  
}