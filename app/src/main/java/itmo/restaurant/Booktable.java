package itmo.restaurant;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TimePicker;

public class Booktable extends Activity {
	EditText edt_name, edt_email, edt_phone, edt_comments;
	Button btn_order, btn_plus, btn_minus, btn_number, btn_date, btn_time;
	String name, email, phone, comment;
	int value = 0;
	private Calendar activeDate;
	private Calendar currentDate;
	String emailhotel, contact;
	PopupMenu popup;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_booktable);

		Gettingintent();
		initialize();
		buttonclick();

	}

	private void initialize() {
		edt_name = (EditText) findViewById(R.id.edt_name);
		edt_email = (EditText) findViewById(R.id.edt_mail);
		edt_phone = (EditText) findViewById(R.id.edt_phone);
		edt_comments = (EditText) findViewById(R.id.edt_comment);

		btn_date = (Button) findViewById(R.id.btn_date);
		btn_time = (Button) findViewById(R.id.btn_time);

		btn_minus = (Button) findViewById(R.id.btn_minus);
		btn_plus = (Button) findViewById(R.id.btn_plus);
		btn_number = (Button) findViewById(R.id.btn_number);
		btn_order = (Button) findViewById(R.id.btn_order);
	}

	private void Gettingintent() {
		Intent iv = getIntent();
		emailhotel = iv.getStringExtra("email");
		contact = iv.getStringExtra("контакт");
		Log.d("записная книига", "" + emailhotel);
	}

	private void buttonclick() {
		// выбираем дату
		btn_date.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar c = Calendar.getInstance();
				int mYear = c.get(Calendar.YEAR);
				int mMonth = c.get(Calendar.MONTH);
				int mDay = c.get(Calendar.DAY_OF_MONTH);
				System.out.println("Выбран " + mDay);
				DatePickerDialog dialog = new DatePickerDialog(Booktable.this,
						new mDateSetListener(), mYear, mMonth, mDay);
				dialog.getDatePicker().setMinDate(
						System.currentTimeMillis() - 1000);
				dialog.show();
			}
		});

		// выбираем время
		btn_time.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Calendar mcurrentTime = Calendar.getInstance();
				int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
				int minute = mcurrentTime.get(Calendar.MINUTE);
				TimePickerDialog mTimePicker;
				mTimePicker = new TimePickerDialog(Booktable.this,
						new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(TimePicker timePicker,
									int selectedHour, int selectedMinute) {
								btn_time.setText(selectedHour + ":"
										+ selectedMinute);
							}
						}, hour, minute, true);// Yes 24 hour time
				mTimePicker.setTitle("Выберите время");
				mTimePicker.show();
			}
		});

		btn_number.setText("" + value);

		// количество людей (кнопка минус)
		btn_minus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (value <= 0) {
					value = 0;
					btn_number.setText("" + value);
				} else {
					value--;
					btn_number.setText("" + value);
				}

			}
		});

		// количество людей (кнопка плюс)
		btn_plus.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				value++;
				btn_number.setText("" + value);
			}
		});

		// валидация почты по регулярным выражениям
		final String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

		// кнопка заказа (по почте)
		btn_order.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				name = edt_name.getText().toString();
				email = edt_email.getText().toString();
				phone = edt_phone.getText().toString();
				comment = edt_comments.getText().toString();

				// валидация
				if (email.matches(emailPattern) && email.length() > 0) {

				} else {
					edt_email.setError("Введите действительный адрес почты");
				}

				if (name.equals("")) {
					edt_name.setError("Введите имя");
				} else if (email.equals("")) {

					edt_email.setError("Введите адрес электронной почты");
				} else if (phone.equals("")) {
					edt_phone.setError("Введите свой номер телефона");
				} else if (value == 0) {
					btn_number.setError("Укажите количество персон");
				} else {
					popup = new PopupMenu(Booktable.this, btn_order);
					popup.getMenuInflater().inflate(R.menu.popbook,
							popup.getMenu());

					// регистрация с выскакивающим сообщением OnMenuItemClickListener
					popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						@Override
						public boolean onMenuItemClick(MenuItem item) {

							switch (item.getItemId()) {
							case R.id.Mail:

								Intent iv = new Intent(Booktable.this,
										Setting.class);
								iv.putExtra("email", "" + emailhotel);
								iv.putExtra("namec", "" + name);
								iv.putExtra("mailid", "" + email);
								iv.putExtra("phone", "" + phone);
								iv.putExtra("comment", "" + comment);
								iv.putExtra("person", "" + btn_number.getText());
								iv.putExtra("date", "" + btn_date.getText());
								iv.putExtra("time", "" + btn_time.getText());

								startActivity(iv);

								return true;
							}

							return true;
						}
					});
					popup.show(); // показываем выскакивающее меню
				}

			}

		});
	}

	// класс выбора даты
	class mDateSetListener implements DatePickerDialog.OnDateSetListener {

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {

			int mYear = year;
			int mMonth = monthOfYear;
			int mDay = dayOfMonth;
			btn_date.setText(new StringBuilder()
					// добавляем 1 к нулевому месяцу
					.append(mMonth + 1).append("/").append(mDay).append("/")
					.append(mYear).append(" "));

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// добавляем в экшн меню
		getMenuInflater().inflate(R.menu.booktable, menu);
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
}
