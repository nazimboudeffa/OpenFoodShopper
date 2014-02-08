package biz.aldaffah.openfoodshopper;

public class Item {

	private String title;
	private String url;

	public Item(CharSequence charSequence1, CharSequence charSequence2) {
		super();
		this.title = (String) charSequence1;
		this.url = (String) charSequence2;
	}

	// getters and setters...

	public CharSequence getTitle() {
		// TODO Auto-generated method stub
		return title;
	}

	public CharSequence getUrl() {
		// TODO Auto-generated method stub
		return url;
	}

	public void setTitle(String t) {
		// TODO Auto-generated method stub
		title = t;
	}

	public void setUrl(String u) {
		// TODO Auto-generated method stub
		url = u;
	}
}
