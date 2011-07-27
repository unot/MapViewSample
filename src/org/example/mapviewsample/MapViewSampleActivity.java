package org.example.mapviewsample;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

public class MapViewSampleActivity extends MapActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        // MapView のインスタンスを取得
        MapView mapView = (MapView)findViewById(R.id.MapView01);
        // MapView をタッチで操作可能にする
        mapView.setClickable(true);
        // タップするとズームコントローラが表示されるようにする
        mapView.setBuiltInZoomControls(true);
        // private クラスとして以降で定義した Overlay の具象クラスを生成
        ConcreteOverlay overlay = new ConcreteOverlay(this);
        // 生成した Overlay を追加する
        List<Overlay> overlayList = mapView.getOverlays();
        overlayList.add(overlay);
    }
    
    /*
     * ルート情報を表示するかどうか
     * @return ルート情報を表示するなら true を返す
     */
    protected boolean isRouteDisplayed() {
    	return false;
    }
    
    /*
     * 地図上に表示されるオーバーレイのクラス
     */
    private class ConcreteOverlay extends Overlay implements OnClickListener {
    	// 円の半径
    	private static final int CIRCLE_RADIUS = 16;
    	// タップされた位置の緯度経度情報を保持するメンバ
    	GeoPoint mGeoPoint;
    	// 円を描画するための色情報
    	Paint mCirclePaint;
    	// 緯度経度と住所情報を相互変換する
    	Geocoder mGeocoder;
    	// コンストラクタ
    	ConcreteOverlay(Context context) {
    		mGeoPoint = null;
    		mCirclePaint = new Paint();
    		// 図形を塗りつぶすことを指定
    		mCirclePaint.setStyle(Paint.Style.FILL);
    		// 図形を不透明の赤色に指定
    		mCirclePaint.setARGB(127, 255, 0, 0);
    		// Geocoder を日本語環境でセット
    		mGeocoder = new Geocoder(context, Locale.JAPAN);
    		// リソースIDからボタンを取得
    		Button button = (Button)findViewById(R.id.Button01);
    		// クリック時の処理を行うリスナーとして自身を登録
    		button.setOnClickListener(this);
    	}
    	// ボタンが押されたときに呼び出される
    	public void onClick(View v) {
    		switch (v.getId()) {
    		// 押されたボタンが Button01 なら
    		case R.id.Button01:
    			// EditText のインスタンスを取得
    			EditText editText = (EditText)findViewById(R.id.EditText01);
    			// EditText の現在の文字列を取得
    			String text = editText.getText().toString();
    			try{
    				// 文字列から住所情報を取得
    				List<Address> addressList = mGeocoder.getFromLocationName(text, 1);
    				if (addressList.size() > 0) {
    					Address address = addressList.get(0);
    					// Address から緯度経度情報を取得しタップ位置にセット
    					setTapPoint(new GeoPoint(
    							(int)(address.getLatitude()*1E6),
    							(int)(address.getLongitude()*1E6)));
    					// MapView のインスタンスを取得
    					MapView mapView = (MapView)findViewById(R.id.MapView01);
    					// 検索結果の位置を画面の中央に
    					mapView.getController().setCenter(mGeoPoint);
    					// MapView の拡大率を変更する
    					mapView.getController().setZoom(15);
    				}
    			} catch (Exception e) { }
    			break;
    		default:
    			break;	
    		}
    	}
    	/*
    	 * MapView をタップされた際に呼び出されるメソッド
    	 * @param point タップされた位置の緯度経度情報
    	 * @param mapView このクラスを保持する MapView への参照
    	 */
    	public boolean onTap(GeoPoint point, MapView mapView) {
    		// タップされた位置の緯度経度情報をメンバにセット
    		setTapPoint(point);
    		// スーパークラスの onTap を呼び出す
    		return super.onTap(point, mapView);
    	}
    	/*
    	 * タップした位置をセットするメソッド
    	 * @param point タップされた位置の緯度経度情報
    	 */
    	private void setTapPoint(GeoPoint point) {
    		mGeoPoint = point;
    		try {
    			// 画面上部の TextView のインスタンスを取得
    			TextView textView = (TextView)findViewById(R.id.TextView01);
    			// 市区町村名まで取得できたかどうか
    			boolean success = false;
    			// 緯度経度から住所情報を取得
    			// 第３引数は、返される検索結果数。1-5の範囲で指定する
    			List<Address> addressList = mGeocoder.getFromLocation(point.getLatitudeE6()/1E6, point.getLongitudeE6()/1E6, 5);
    			// 検索結果を順に処理
    			for (Iterator<Address> it=addressList.iterator(); it.hasNext();) {
    				Address address = it.next();
    				// 国名を取得
    				String country = address.getCountryName();
    				// 都道府県名を取得
    				String admin = address.getAdminArea();
    				// 市区町村名を取得
    				String locality = address.getLocality();
    				// 市区町村名まで取得できていれば TextView を更新
    				if (country != null && admin != null && locality != null) {
    					textView.setText(country + admin + locality);
    					success = true;
    					break;
    				}
    			}
    			// 取得に失敗していれば、TextView をエラー表記に変更
    			if (!success) textView.setText("Error");
    			// TextView の再描画を行う（変更を反映させるため）
    			textView.invalidate();
    		} catch (Exception e) { }
    		// 緯度経度情報を用いて Weather API にアクセス
    		getWeatherXML(point);
    	}
    	/*
    	 * この Overlay を保持する MapView の描画時に呼び出される
    	 * @param canvas
    	 * @param mapView このクラスを保持する MapView への参照
    	 * @param shadow 影の描画のために draw が呼び出されたかどうか。
    	 * 
    	 * draw メソッドは、通常の描画と影の描画のために二度呼び出される
    	 * shadow はそれぞれ、通常の描画なら false, 影の描画なら true となる
    	 */
    	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
    		// スーパークラスの draw メソッドを呼び出す
    		super.draw(canvas, mapView, shadow);
    		// 今回は、影描画は行わないので、影でない場合のみ処理
    		if (!shadow) {
    			// タップした緯度経度情報が存在する場合のみ処理
    			if (mGeoPoint != null) {
    				// 地図上の緯度経度から、Canvas の座標系へと変換する
    				Projection projection = mapView.getProjection();
    				Point point = new Point();
    				projection.toPixels(mGeoPoint, point);
    				// 円を描画する
    				canvas.drawCircle(point.x, point.y, CIRCLE_RADIUS, mCirclePaint);
    			}
    		}
    	}
    	/*
    	 * Weather API ( http://www.worldweatheronline.com/ ) を利用して
    	 * 天気情報を取得し、ビューに反映させる
    	 * @param point 緯度経度情報
    	 */
    	private void getWeatherXML (GeoPoint point) {
    		byte[] xml_byte = null;
    		try {
    			// 10進数の Format オブジェクト
    			DecimalFormat df = new DecimalFormat();
    			// 数値だけを出力するよう指定
    			df.applyPattern("0");
    			// 小数点第2位まで出力するよう指定
    			df.setMinimumFractionDigits(2);
    			df.setMaximumFractionDigits(2);
    			// 緯度と経度を文字列に変換
    			String latitude_str = df.format(point.getLatitudeE6()/1E6);
    			String longitude_str = df.format(point.getLongitudeE6()/1E6);
    			// Weather API の URI
    			String weather_url = "http://www.worldweatheronline.com/feed/weather.ashx?q=" +
    					latitude_str + "." + longitude_str + 
    					"&format=xml&num_of_days=1&key=a3827b18b1023422112707";
    			// Weather API から天気情報XMLを取得
    			xml_byte = getHttp(weather_url);
    		} catch (Exception e) { }
    		if(xml_byte == null) return;
    		// XMLをパースする
    		parseXml(xml_byte);
    	}
    	/*
    	 * HTTP GET でデータを取得し、byte列として返す
    	 * @param url_str URL文字列
    	 * @return 取得したデータ。取得に失敗したら null が返される 
    	 */
    	private byte[] getHttp(String url_str) {
    		// HTTP接続
    		HttpURLConnection connect = null;
    		// 入力ストリーム
    		InputStream istream = null;
    		// byte型配列出力ストリーム
    		ByteArrayOutputStream ostream = null;
    		// 結果として返す byte 型配列
    		byte[] result = null;
    		try{
    			// URLオブジェクトを生成
    			URL url = new URL(url_str);
    			// HTTP接続用オブジェクトを生成
    			connect = (HttpURLConnection)url.openConnection();
    			// HTTPリクエストをGETにセット
    			connect.setRequestMethod("GET");
    			// 入力ストリームオブジェクトを取得
    			istream = connect.getInputStream();
    			// 出力ストリームオブジェクトを生成
    			ostream = new ByteArrayOutputStream();
    			byte[] buf = new byte[1024];
    			while(true) {
    				// 入力ストリームからデータを取得
    				int size = istream.read(buf);
    				// 取得するデータがなくなったらループ終了
    				if(size <= 0) break;
    				// 取得したデータを出力ストリームに書きこむ
    				ostream.write(buf, 0, size);
    			}
    			// byte型の配列としてデータを取り出す
    			result = ostream.toByteArray();
    		} catch (Exception e) { }
    		finally {
    			// HTTP接続を切断
    			if(connect != null)
    				connect.disconnect();
    			try {
    				// 入力ストリームを閉じる
    				if (istream != null)
    					istream.close();
    				// 出力ストリームを閉じる
    				if (ostream != null)
    					ostream.close();
    			} catch (Exception e) { }
    		}
    		return result;
    	}
    	/*
    	 * Weather API から取得した天気情報をXMLを解析し、ビューに反映させる
    	 * @param xml_byte 天気情報XMLのバイト列
    	 */
    	private void parseXml (byte[] xml_byte) {
    		// XMLParserとしてXmlPullParserを利用
    		XmlPullParser parser = Xml.newPullParser();
    		// 摂氏温度
			String temp_c = null;
			// 天気のアイコンURL
			String weather_icon_url = null;
    		try {
    			// XMLパーサに引数のバイト列をセット
    			parser.setInput(new StringReader(new String(xml_byte, "UTF-8")));
    			// 最初の要素を取得
    			int eventType = parser.getEventType();
    			// 現在位置が current_conditon タグ内か
    			boolean tag_current_condition = false;
    			// 最後まで読まれるか、摂氏とアイコンURLの両方が取得できたら終了
    			while(eventType != XmlPullParser.END_DOCUMENT && (temp_c == null || weather_icon_url == null)) {
    				switch(eventType) {
    				// タグの先頭
    				case XmlPullParser.START_TAG:
    					// current_conditoin タグ内に入っているかどうか
    					if(parser.getName().equals("current_condition"))
    						tag_current_condition = true;
    					// current_condition タグ内の
    					if(tag_current_condition) {
    						// Temp_C タグ（摂氏の気温）か
    						if(parser.getName().equals("temp_C")) {
    							// パーサを進めて
    							eventType = parser.next();
    							if(eventType == XmlPullParser.TEXT) {
    								// テキストを取得
    								temp_c = parser.getText();
    							}
    						}
    						// weatherIconUrl タグなら
    						else if(parser.getName().equals("weatherIconUrl")) {
    							// パーサを進めて
    							eventType = parser.next();
    							if(eventType == XmlPullParser.TEXT) {
    								// テキストを取得
    								weather_icon_url = parser.getText();
    							}
    						}
    					}
    					break;
    					// タグの末尾
    				case XmlPullParser.END_TAG:
    					// current_condition タグから出たかどうか
    					if(parser.getName().equals("current_condition"))
    						tag_current_condition = false;
    					break;
    				}
    				eventType = parser.next();
    			}
    		} catch (Exception e) { }
    		
    		if(weather_icon_url != null) {
    			// 天気情報アイコンを取得
    			byte[] byteArray = getHttp(weather_icon_url);
    			// 天気情報のBMPを取得
    			Bitmap weather_icon_bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    			// 天気情報のアイコンを表示する ImageView のインスタンスを取得
    			ImageView imageView = (ImageView)findViewById(R.id.ImageView01);
    			imageView.setImageBitmap(weather_icon_bitmap);
    			// ImageView の再描画を行う（変更を反映させるため）
    			imageView.invalidate();
    		}
    		// 気温を表示する TextView のインスタンスを取得
    		TextView textView = (TextView)findViewById(R.id.TextView02);
    		// 気温をセット
    		if(temp_c != null)
    			textView.setText(temp_c + "℃");
    		else
    			textView.setText("Error");
    		// TextView の再描画
    		textView.invalidate();
    	}
    }
}