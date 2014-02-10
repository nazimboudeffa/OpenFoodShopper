/****************************************************************************
 *                                                                          *
 * Open Food Shopper - Open Source Android Application                      *
 * Search food in Open Food Facts and add it in OI Shopping List            *
 *                                                                          *
 ****************************************************************************
 * Copyright (C) 2014 Al Daffah Consulting. (http://www.aldaffah.biz)       *
 *                                                                          *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 *                                                                          *
 *      http://www.apache.org/licenses/LICENSE-2.0                          *
 *                                                                          *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 ****************************************************************************
 *                                                                          *
 * @author Nazim Boudeffa                                                   *
 * And some googling                                                        *
 * Thanks to Stéphane Gigandet from OpenFoodFacts.org                       *
 * Thanks to Openintent.og for Opening OI Applications                      *
 *                                                                          *
 ****************************************************************************/
package biz.aldaffah.openfoodshopper;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class SearchActivity extends Activity {
	/**
	 * Inserts shopping list items from a string array in intent extras.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.STRING_ARRAYLIST_SHOPPING"
	 * </p>
	 */
	public static final String EXTRA_STRING_ARRAYLIST_SHOPPING = "org.openintents.extra.STRING_ARRAYLIST_SHOPPING";

	/**
	 * Intent extra for list of quantities corresponding to shopping list items
	 * in STRING_ARRAYLIST_SHOPPING.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.STRING_ARRAYLIST_QUANTITY"
	 * </p>
	 */
	public static final String EXTRA_STRING_ARRAYLIST_QUANTITY = "org.openintents.extra.STRING_ARRAYLIST_QUANTITY";

	/**
	 * Intent extra for list of prices corresponding to shopping list items in
	 * STRING_ARRAYLIST_SHOPPING.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.STRING_ARRAYLIST_PRICE"
	 * </p>
	 */
	public static final String EXTRA_STRING_ARRAYLIST_PRICE = "org.openintents.extra.STRING_ARRAYLIST_PRICE";

	/**
	 * Intent extra for list of barcodes corresponding to shopping list items in
	 * STRING_ARRAYLIST_SHOPPING.
	 * 
	 * <p>
	 * Constant Value: "org.openintents.extra.STRING_ARRAYLIST_BARCODE"
	 * </p>
	 */
	public static final String EXTRA_STRING_ARRAYLIST_BARCODE = "org.openintents.extra.STRING_ARRAYLIST_BARCODE";

	// Add extras an call OI Shopping Activity
	ArrayList<String> extraShopping = new ArrayList<String>();
	ArrayList<String> extraQuantity = new ArrayList<String>();
	ArrayList<String> extraPrice = new ArrayList<String>();
	ArrayList<String> extraBarcode = new ArrayList<String>();

	private static final String TAG = "OpenFoodShopper";
	private int limit = 5;
	public String texttosearch;
	public ArrayList<Item> mList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		mList = new ArrayList<Item>();
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {

			try {
				/*
				 * Get the Bundle Object Bundle bundleObject =
				 * getIntent().getExtras(); Get ArrayList Bundle ArrayList<Item>
				 * classObject = ((ArrayList<Item>)
				 * bundleObject.getSerializable("ITEM_LIST")); It's better to
				 * get only texttosearch for a better Device performance
				 */

				Bundle bundle = getIntent().getExtras();

				if (bundle.getString("TTS") != null) {
					texttosearch = bundle.getString("TTS");
					new SearchTask().execute();

				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			Context context = getApplicationContext();
			CharSequence text = "Pas de connexion réseau !";
			int duration = Toast.LENGTH_SHORT;

			Toast toast = Toast.makeText(context, text, duration);
			toast.show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private class SearchTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... arg0) {

			// TODO Auto-generated method stub
			try {
				Log.i(TAG, "doInBackground");

				/*
				 * TODO Check if Open Food Facts MongoDB is empty or in
				 * maintenance before connect
				 */
				MongoClient mongo = new MongoClient("paulo.mongohq.com", 10096);
				DB db = mongo.getDB("OpenFoodFacts");
				db.authenticate("perfectshopper", "passive".toCharArray());

				DBCollection table = db.getCollection("foodz");
				BasicDBObject searchQuery = new BasicDBObject();

				searchQuery.put("product_name", new BasicDBObject("$regex",
						"(.*)" + texttosearch + "(.*)"));
				DBCursor cursor = table.find(searchQuery).limit(limit);

				try {
					while (cursor.hasNext()) {
						DBObject Result = cursor.next();
						mList.add(new Item((String) Result.get("product_name"),
								(String) Result.get("url")));

					}
					;
				} finally {
					cursor.close();
				}

			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				Log.e(TAG, "doInBackground");
				e.printStackTrace();
			}
			return null;
		}

		// onPostExecute displays the results of the AsyncTask.
		@Override
		protected void onPostExecute(String result) {
			Log.i(TAG, "PostExecute");
			if (!mList.isEmpty()) {
				displayFoodList(mList);
			} else {
				Context context = getApplicationContext();
				CharSequence text = "On ne trouve pas ";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text + texttosearch,
						duration);
				toast.show();
			}

		}

	}

	public void displayFoodList(final ArrayList<Item> List) {

		// 1. pass context and data to the custom adapter
		MyAdapter mAdapter = new MyAdapter(this, List);

		// 2. Get ListView from activity_main.xml
		ListView listView = (ListView) findViewById(R.id.listView);

		// 3. setListAdapter
		listView.setAdapter(mAdapter);

		final Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				List.clear();
				limit = limit + 5;
				new SearchTask().execute();

			}
		});

		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String url = List.get(position).getUrl().toString();

				// Item String URL from Open Food Facts Database
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);

			}
		});

		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int pos, long id) {

				/*
				 * Passing Extras to ArrayLists then call startApplication to
				 * add them to OI Shopping List It's better to manage quantities
				 * directly from Shopping
				 */

				extraShopping.add(List.get(pos).getTitle().toString());
				extraQuantity.add("1");
				extraPrice.add("0");
				extraBarcode.add("0");

				if (startApplication("org.openintents.shopping")) {

					Context context = getApplicationContext();
					CharSequence text = List.get(pos).getTitle().toString();
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, "Ajout de " + text,
							duration);
					toast.show();

					/*
					 * After adding items clear the ArrayLists TODO verify if
					 * the items exist in ArrayLists or not
					 */

					extraShopping.clear();
					extraQuantity.clear();
					extraPrice.clear();
					extraBarcode.clear();

				} else {
					/*
					 * No match, so OI Shopping is not installed verify and add
					 * it from market
					 */

					showInMarket("org.openintents.shopping");
				}
				;

				return true;

			}
		});

	}

	public boolean startApplication(String packageName) {
		try {

			Intent intent = new Intent("android.intent.action.MAIN");
			intent.addCategory("android.intent.category.LAUNCHER");

			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
			List<ResolveInfo> resolveInfoList = getPackageManager()
					.queryIntentActivities(intent, 0);

			for (ResolveInfo info : resolveInfoList)
				if (info.activityInfo.packageName.equalsIgnoreCase(packageName)) {
					launchComponent(info.activityInfo.packageName,
							info.activityInfo.name);
					return true;
				}

		} catch (Exception e) {
			Log.e("ERROR", "Undtermined error !");
		}
		return false;
	}

	private void launchComponent(String packageName, String name) {

		Intent intent = new Intent();
		/*
		 * TODO FIX Creation of menu in shopping if (mExtraItems != null) {
		 * menu.add(0, MENU_INSERT_FROM_EXTRAS, 0, R.string.menu_auto_add)
		 * .setIcon(android.R.drawable.ic_menu_upload); }
		 */

		/*
		 * clear my thoughts about intents and the call to startApplication
		 * intent.setComponent(new ComponentName(packageName,
		 * packageName+".ShoppingActivity")); intent.setClassName(packageName,
		 * packageName + ".ui.ShoppingListsActivity"); Intent is found
		 * automatically so it have to be called directly
		 */

		intent.setType("org.openintents.type/string.arraylist.shopping");
		intent.setAction("org.openintents.action.INSERT_FROM_EXTRAS");

		intent.putStringArrayListExtra(EXTRA_STRING_ARRAYLIST_SHOPPING,
				extraShopping);
		intent.putStringArrayListExtra(EXTRA_STRING_ARRAYLIST_QUANTITY,
				extraQuantity);
		intent.putStringArrayListExtra(EXTRA_STRING_ARRAYLIST_PRICE, extraPrice);
		intent.putStringArrayListExtra(EXTRA_STRING_ARRAYLIST_BARCODE,
				extraBarcode);

		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		startActivity(intent);
	}

	private void showInMarket(String packageName) {
		Uri uri = Uri.parse("market://details?id=" + packageName);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e("ERROR", "Google Play Market not found!");
		}

	}

}
