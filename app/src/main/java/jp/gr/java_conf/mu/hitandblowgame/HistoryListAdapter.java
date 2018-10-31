package jp.gr.java_conf.mu.hitandblowgame;

import java.util.ArrayList;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
public class HistoryListAdapter extends BaseAdapter {

	private LayoutInflater layoutInflater = null;
	private ArrayList<HistoryRowData> historyList = null;

	public HistoryListAdapter(Context context,
			ArrayList<HistoryRowData> historyList) {

		// LayoutInflaterを取得
		layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.historyList = historyList;
	}

	/*
	 * リストの1行が表示（再表示）されるごとに、getView()が呼び出されるので、行位置（position）に応じて各idの値をセットする。
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			// 1行分のレイアウトを生成
			convertView = layoutInflater
					.inflate(R.layout.history_rowdata, null);
		} else {
			// 再利用される場合の処理はここに入る
		}

		// 各idの項目に値をセット
		TextView no = (TextView) convertView.findViewById(R.id.historyNo);
		TextView answer = (TextView) convertView
				.findViewById(R.id.historyAnswer);
		TextView hit = (TextView) convertView.findViewById(R.id.historyHit);
		TextView blow = (TextView) convertView.findViewById(R.id.historyBlow);

		no.setText(historyList.get(position).getNo() + "");
		answer.setText(historyList.get(position).getAnswer());
		hit.setText(historyList.get(position).getHit() + "");
		blow.setText(historyList.get(position).getBlow() + "");

		return convertView;
	}

	/**
	 * 要素を追加する
	 *
	 * @param idx
	 *            挿入位置
	 * @param rowData
	 *            挿入対象の要素
	 */
	public void insert(int idx,HistoryRowData rowData){
		historyList.add(0, rowData);
	}

	/**
	 * 要素をリセットする
	 *
	 */
	public void clear(){
		historyList.clear();
	}

	@Override
	public int getCount() {
		return historyList.size();
	}

	@Override
	public Object getItem(int position) {
		return historyList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}