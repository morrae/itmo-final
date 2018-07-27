package itmo.restaurant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import itmo.getset.Restgetset;
import itmo.util.AlertDialogManager;
import itmo.util.ConnectionDetector;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Category extends Activity {
	Button btn_more, btn_map, btn_more1;
	ListView list_category;
	LinearLayout lldisplay;
	ProgressDialog progressDialog;
	ArrayList<Restgetset> rest;
	Boolean isInternetPresent = false;
	ConnectionDetector cd;
	View layout12;
	private String Error = null;
	InterstitialAd mInterstitialAd;
	boolean interstitialCanceled;
	AlertDialogManager alert = new AlertDialogManager();
	int MainPosition = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category);

		cd = new ConnectionDetector(getApplicationContext());

		isInternetPresent = cd.isConnectingToInternet();
		// проверяем есть ли интернет
		if (isInternetPresent) {
			if (getString(R.string.category_banner).equals("yes")) {
				AdView mAdView = (AdView) findViewById(R.id.adView);
				AdRequest adRequest = new AdRequest.Builder().build();
				mAdView.loadAd(adRequest);
			}
			Initialize();
		} else {
			// Если нет подключения, то просим пользователя подключиться
			RelativeLayout rl_back = (RelativeLayout) findViewById(R.id.rl_back);
			if (rl_back == null) {
				Log.d("second", "second");
				RelativeLayout rl_dialoguser = (RelativeLayout) findViewById(R.id.rl_infodialog);

				layout12 = getLayoutInflater().inflate(
						R.layout.connectiondialog, rl_dialoguser, false);

				rl_dialoguser.addView(layout12);
				rl_dialoguser.startAnimation(AnimationUtils.loadAnimation(
						Category.this, R.anim.popup));
				Button btn_yes = (Button) layout12.findViewById(R.id.btn_yes);
				btn_yes.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						finish();
					}
				});
			}
		}

	}

	private void Initialize() {
		rest = new ArrayList<Restgetset>();
		new getcategorydetail().execute();
	}

	// класс категорий
	public class getcategorydetail extends AsyncTask<Void, Void, Void> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressDialog = new ProgressDialog(Category.this);
			progressDialog.setMessage("Загрузка...");
			progressDialog.setCancelable(true);
			progressDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			getdetailforNearMe();
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			if (Error != null) {
				RelativeLayout rl_back = (RelativeLayout) findViewById(R.id.rl_back);
				if (rl_back == null) {
					Log.d("second", "second");
					RelativeLayout rl_dialoguser = (RelativeLayout) findViewById(R.id.rl_infodialog);

					layout12 = getLayoutInflater().inflate(
							R.layout.json_dilaog, rl_dialoguser, false);

					rl_dialoguser.addView(layout12);
					rl_dialoguser.startAnimation(AnimationUtils.loadAnimation(
							Category.this, R.anim.popup));
					Button btn_yes = (Button) layout12
							.findViewById(R.id.btn_yes);
					btn_yes.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							finish();
						}
					});
				}
			} else {
				if (progressDialog.isShowing()) {
					progressDialog.dismiss();

					list_category = (ListView) findViewById(R.id.list_detail);

					LazyAdapter lazy = new LazyAdapter(Category.this, rest);
					list_category.setAdapter(lazy);

					list_category
							.setOnItemClickListener(new OnItemClickListener() {

								@Override
								public void onItemClick(AdapterView<?> parent,
										View view, int position, long id) {
									MainPosition = position;
									if (interstitialCanceled) {

									} else {

										if (mInterstitialAd != null
												&& mInterstitialAd.isLoaded()) {
											mInterstitialAd.show();
										} else {

											ContinueIntent();
										}
									}

								}
							});

				}
			}

		}
	}


	private void getdetailforNearMe() {

		URL hp = null;
		try {

			 hp = new URL(
			 getString(R.string.liveurl)+"foodcategory.php");

			Log.d("URL", "" + hp);
			URLConnection hpCon = hp.openConnection();
			hpCon.connect();
			InputStream input = hpCon.getInputStream();
			Log.d("input", "" + input);

			BufferedReader r = new BufferedReader(new InputStreamReader(input));

			String x = "";
			x = r.readLine();
			String total = "";

			while (x != null) {
				total += x;
				x = r.readLine();
			}
			Log.d("URL", "" + total);

			JSONArray j = new JSONArray(total);

			Log.d("URL1", "" + j);
			for (int i = 0; i < j.length(); i++) {

				JSONObject Obj;
				Obj = j.getJSONObject(i);

				Restgetset temp = new Restgetset();
				temp.setTitle(Obj.getString("name"));
				temp.setImage(Obj.getString("image"));

				rest.add(temp);
			}

		} catch (MalformedURLException e) {
			e.printStackTrace();
			Error = e.getMessage();
		} catch (IOException e) {
			e.printStackTrace();
			Error = e.getMessage();
		} catch (JSONException e) {
			Error = e.getMessage();
			e.printStackTrace();
		} catch (NullPointerException e) {
			Error = e.getMessage();
		}
	}

	public class LazyAdapter extends BaseAdapter {

		private Activity activity;
		private ArrayList<Restgetset> data;
		private LayoutInflater inflater = null;
		Typeface tf2 = Typeface.createFromAsset(Category.this.getAssets(),
				"fonts/calibri.ttf");

		public LazyAdapter(Activity a, ArrayList<Restgetset> str) {
			activity = a;
			data = str;
			inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View vi = convertView;

			if (convertView == null) {

				vi = inflater.inflate(R.layout.cellcat, null);
			}

			TextView txt_name = (TextView) vi.findViewById(R.id.txt_cat);
			txt_name.setText(data.get(position).getTitle());
			txt_name.setTypeface(tf2);

			String image = data.get(position).getImage();
			Log.d("image", "" + image);

			ImageView img_cat = (ImageView) vi.findViewById(R.id.img_cat);
			img_cat.setImageResource(R.drawable.categories_page_img);
			 new DownloadImageTask(img_cat)
			 .execute(getString(R.string.liveurl)+"uploads/category/"
			 + image);
			return vi;
		}
	}

	class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;
		Bitmap mIcon11;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

		}

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		@Override
		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];

			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
				mIcon11 = Bitmap.createScaledBitmap(mIcon11, 70, 50, true);
			} catch (Exception e) {
				Log.e("Ошибка", "" + e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		@Override
		protected void onPostExecute(Bitmap result) {

			bmImage.setImageBitmap(result);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		/*
		 * Intent iv = new Intent(Category.this, Home.class);
		 * iv.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
		 * Intent.FLAG_ACTIVITY_NEW_TASK); startActivity(iv);
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.category, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		super.onResume();

		interstitialCanceled = false;
		CallNewInsertial();

	}

	private void CallNewInsertial() {
		cd = new ConnectionDetector(Category.this);

		if (!cd.isConnectingToInternet()) {
			alert.showAlertDialog(Category.this, "Ошибка подключения",
					"Пожалуйста, подключитесь к инетрнету", false);
			return;
		} else {
			if (getString(R.string.Category_show_ads).equals("да")) {
				Log.d("ad", "" + getString(R.string.Home_show_ads));
				mInterstitialAd = new InterstitialAd(this);
				mInterstitialAd
						.setAdUnitId(getString(R.string.insertial_ad_key));

				requestNewInterstitial();

				mInterstitialAd.setAdListener(new AdListener() {
					@Override
					public void onAdClosed() {
						ContinueIntent();
					}

				});
			} else {
				ContinueIntent();
			}

		}
	}

	private void ContinueIntent() {
		Intent iv = new Intent(Category.this, Home.class);
		iv.putExtra("foodname", "" + rest.get(MainPosition).getTitle());
		startActivity(iv);
	}

	private void requestNewInterstitial() {
		AdRequest adRequest = new AdRequest.Builder().build();

		mInterstitialAd.loadAd(adRequest);

	}

	@Override
	public void onPause() {
		mInterstitialAd = null;
		interstitialCanceled = true;
		super.onPause();
	}
}
