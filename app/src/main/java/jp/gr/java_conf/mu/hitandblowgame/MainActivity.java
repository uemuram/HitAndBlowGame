package jp.gr.java_conf.mu.hitandblowgame;

import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

    // 自分自身(AlertDialogに渡すために定義しておく)
    private Activity me;
    // 正解(4桁の数字)
    private String[] correctAnswer;
    // 解答履歴を保持するアダプタ
    private HistoryListAdapter historyListAdapter;
    // 解答欄のView
    private EditText[] answerColumn;
    // 正解が表示されるView
    private TextView[] correctAnswerColumn;
    // ゲーム中かどうかを判別するフラグ
    private boolean playing;
    // データ保存、読み込み用プリファレンス
    private SharedPreferences pref;

    // チェックボタンが押下されたときのイベントを定義するリスナー
    private final OnClickListener onClickCheckButtonListner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // ユーティリティを準備
            Utility u = new Utility();
            // 解答欄に入力された値を取得し、現在の解答として配列に保存
            String answer[] = new String[4];
            for (int i = 0; i < 4; i++) {
                answer[i] = answerColumn[i].getText().toString();
            }
            // --------入力チェック--------
            // 入力チェックエラー有無
            boolean error = false;
            // エラーメッセージ
            String errorMsg = "";
            if (u.nullCheck(answer) >= 0) {
                // 空の入力欄があった場合
                errorMsg = "未入力の欄があります";
                error = true;
            } else if (u.duplicateCheck(answer) >= 0) {
                // 重複があった場合
                errorMsg = "数字が重複しています";
                error = true;
            }
            // 入力エラーがあった場合はアラートを出して終了
            if (error) {
                AlertDialog.Builder ab = new AlertDialog.Builder(me);
                ab.setMessage(errorMsg);
                ab.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // 現時点では空実装
                                // エラーがある入力欄にカーソルを合わせる処理を書くのが親切かもしれない
                            }
                        });
                ab.show();
                return;
            }
            // ----------------------------------

            // 今回分の解答履歴を生成
            HistoryRowData rowData = createRowData(answer);
            // 解答履歴をリストビューの先頭に追加し、リストをリフレッシュ(アダプタに更新を通知)
            historyListAdapter.insert(0, rowData);
            historyListAdapter.notifyDataSetChanged();

            // 正解だった場合(4Hitだった場合)
            if (rowData.getHit() == 4) {
                // ゲームクリア
                clearGame();
            }
            return;
        }

        private HistoryRowData createRowData(String[] answer) {
            // Hit数、Blow数
            int hit = 0, blow = 0;
            // 解答と正解を比較してHit数、Blow数カウント
            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    if (answer[i].equals(correctAnswer[j])) {
                        if (i == j) {
                            // 数字と位置が一致していた場合はHit
                            hit++;
                        } else {
                            // 数字のみ一致していた場合はBlow
                            blow++;
                        }
                    }
                }
            }
            // 解答履歴を生成
            HistoryRowData rowData = new HistoryRowData();
            rowData.setNo(historyListAdapter.getCount() + 1);
            rowData.setAnswer(answer[0] + answer[1] + answer[2] + answer[3]);
            rowData.setHit(hit);
            rowData.setBlow(blow);
            return rowData;
        }
    };

    // リプレイボタンが押下されたときのイベントを定義するリスナー
    private final OnClickListener onClickReplayButtonListner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // 解答履歴をクリア
            historyListAdapter.clear();
            historyListAdapter.notifyDataSetChanged();
            // チェックボタンの有効化、リプレイボタンの無効化
            findViewById(R.id.checkButton).setVisibility(View.VISIBLE);
            findViewById(R.id.replayButton).setVisibility(View.INVISIBLE);
            // フェードアウト、フェードインのアニメーションを取得
            Animation animFadeOut = AnimationUtils.loadAnimation(me,
                    R.anim.answer_fadeout);
            Animation animFadeIn = AnimationUtils.loadAnimation(me,
                    R.anim.answer_fadein);
            animFadeOut.setDuration(20);
            animFadeIn.setDuration(20);
            // アニメーションをセット
            for (int i = 0; i < 4; i++) {
                // 解答欄をクリアしてフェードイン
                answerColumn[i].getEditableText().clear();
                answerColumn[i].startAnimation(animFadeIn);
                // 正解をフェードアウト
                correctAnswerColumn[i].startAnimation(animFadeOut);
            }
            // ゲーム開始
             startGame();
        }
    };

    // 解答欄がタッチされたときのイベントを定義するリスナー
    private final OnTouchListener onTouchAnswerColumnListner = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // onTouchイベントは、指が触れたとき、離れたときの2回発生するので、
            // 触れたとき(ACTION_DOWN)に処理実施
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                TextView t = (TextView) v;
                // 解答欄をクリアする
                if (t.getText().length() > 0) {
                    t.getEditableText().clear();
                }
            }
            return false;
        }
    };

    // 解答欄が変更されたときのイベントを定義するリスナー(「次のフォーカス先」の情報を持つTextWather)
    private class OnChangeAnswerColumnListner implements TextWatcher {
        // テキスト変更が発生したTextView
        private TextView thisView;
        // 次のフォーカス先
        private View nextFocus = null;

        // コンストラクタ。次フォーカス先を記録
        public OnChangeAnswerColumnListner(TextView thisView, View nextFocus) {
            this.thisView = thisView;
            this.nextFocus = nextFocus;
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 1 && nextFocus != null) {
                // 1文字の入力があったときは、次の入力欄にフォーカスを移す
                nextFocus.requestFocus();
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (s.length() == 2) {
                // 2文字目の入力があったときは、2文字目で1文字目を置き換える
                // (EditTextを削除せずに文字入力するための対応)
                thisView.setText(s.toString().substring(start, start + 1));
            }
        }
    }

    // アクティビティ生成時
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.me = this;

        // --------初期設定(1) 画面レイアウト整備--------
        // メイン画面のレイアウト(入力欄、ボタン、リストビュー含む)をセット
        setContentView(R.layout.activity_main);
        // 解答履歴を保持するリストビュー用のアダプタ
        historyListAdapter = new HistoryListAdapter(this,
                new ArrayList<HistoryRowData>());
        // リストビューを取得してアダプタを登録
        ((ListView) findViewById(R.id.historyList))
                .setAdapter(historyListAdapter);
        // ------------------------------------

        // --------初期設定(2) データ整備--------
        // 解答欄を配列にセット
        answerColumn = new EditText[4];
        answerColumn[0] = (EditText) findViewById(R.id.answerColumn0);
        answerColumn[1] = (EditText) findViewById(R.id.answerColumn1);
        answerColumn[2] = (EditText) findViewById(R.id.answerColumn2);
        answerColumn[3] = (EditText) findViewById(R.id.answerColumn3);
        // 正解欄を配列にセット
        correctAnswerColumn = new TextView[4];
        correctAnswerColumn[0] = (TextView) findViewById(R.id.correctAnswerColumn0);
        correctAnswerColumn[1] = (TextView) findViewById(R.id.correctAnswerColumn1);
        correctAnswerColumn[2] = (TextView) findViewById(R.id.correctAnswerColumn2);
        correctAnswerColumn[3] = (TextView) findViewById(R.id.correctAnswerColumn3);
        // 正解を保持する配列を用意
        correctAnswer = new String[4];
        // 成績保存、参照用のプリファレンスを用意
        pref = getSharedPreferences("hbRecord", MODE_PRIVATE);
        // ------------------------------------

        // --------初期設定(3) イベント設定--------
        // チェックボタンをクリックした際のイベントを登録
        findViewById(R.id.checkButton).setOnClickListener(
                onClickCheckButtonListner);
        // リプレイボタンをクリックした際のイベントを登録
        findViewById(R.id.replayButton).setOnClickListener(
                onClickReplayButtonListner);
        // 解答欄へのイベント登録
        EditText nextFocus;
        for (int i = 0; i < 4; i++) {
            // タッチされた時は解答欄をクリアする
            answerColumn[i].setOnTouchListener(onTouchAnswerColumnListner);
            // 解答欄に入力があった際のフォーカスの移動先(右隣)
            nextFocus = answerColumn[(i + 1) % 4];
            // 入力があった時はフォーカスを移動する
            answerColumn[i]
                    .addTextChangedListener(new OnChangeAnswerColumnListner(
                            answerColumn[i], nextFocus));
        }
        // ------------------------------------
        // ゲーム開始
        startGame();
    }

    // メニュー生成時
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // リソースで定義済みのメニューを取得、設定
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    // メニュー表示時
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // ギブアップメニューの表示設定
        MenuItem giveUpMenu = (MenuItem) menu.findItem(R.id.menu_giveUp);
        if (playing) {
            // ゲーム中であれば、ギブアップメニューを表示
            giveUpMenu.setVisible(true);
        } else {
            // ゲーム中でなければ、ギブアップメニューを非表示
            giveUpMenu.setVisible(false);
        }
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    // メニュー選択時
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder ab = new AlertDialog.Builder(me);
        switch (item.getItemId()) {
            case R.id.menu_giveUp:
                // ギブアップ処理
                ab.setMessage("ギブアップしますか?");
                ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 正解を表示してゲーム終了
                        endGame();
                    }
                });
                ab.setNegativeButton("キャンセル", null);
                ab.show();
                break;

            case R.id.menu_record:
                // 成績表示処理
                 dispRecord();
                break;

            case R.id.menu_quit:
                // ゲーム終了処理
                ab.setMessage("ゲームを終了しますか?");
                ab.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        me.finish();
                    }
                });
                ab.setNegativeButton("キャンセル", null);
                ab.show();
                break;
        }
        return true;
    }

    // ゲーム開始処理
    private void startGame() {
        // 1つ目の解答欄にフォーカスを合わせる
        answerColumn[0].requestFocus();
        // 正解を計算
        setCorrectAnswer();
        // ゲーム回数をインクリメント
        long gameCount = pref.getLong("gameCount", 0);
        gameCount++;
        Editor e = pref.edit();
        e.putLong("gameCount", gameCount);
        e.commit();
        // ゲーム中にする
        playing = true;
    }

    // ゲームクリア処理
    private void clearGame() {
        // クリア回数を取得
        long clearCount = pref.getLong("clearCount", 0);
        // 正解までの平均回答数を取得
        float answerAvg = pref.getFloat("answerAvg", -1);
        // 今回の回答数を取得
        float nowAnswerCount = (float) (historyListAdapter.getCount());
        // 平均回答数を更新
        answerAvg = ((float) clearCount * answerAvg + nowAnswerCount)
                / ((float) clearCount + 1);
        // クリア回数をインクリメント
        clearCount++;
        // クリア回数、平均回答数を登録
        Editor e = pref.edit();
        e.putLong("clearCount", clearCount);
        e.putFloat("answerAvg", answerAvg);
        e.commit();

        // ソフトウェアキーボードを隠す
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus()
                .getWindowToken(), 0);
        // 正解を表示してゲーム終了
        endGame();
        // アラート表示
        AlertDialog.Builder ab = new AlertDialog.Builder(me);
        ab.setMessage("正解!");
        ab.setPositiveButton("OK", null);
        ab.show();
    }

    // ゲーム終了処理(正解表示など)
    private void endGame() {
        // ゲーム中を解除
        playing = false;
        // チェックボタンの無効化、リプレイボタンの有効化
        findViewById(R.id.checkButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.replayButton).setVisibility(View.VISIBLE);
        // フェードアウト、フェードインのアニメーションを準備
        Animation[] animFadeOut = new Animation[4];
        Animation[] animFadeIn = new Animation[4];
        for (int i = 0; i < 4; i++) {
            // アニメーションをXMLから取得
            animFadeOut[i] = AnimationUtils.loadAnimation(me,
                    R.anim.answer_fadeout);
            animFadeIn[i] = AnimationUtils.loadAnimation(me,
                    R.anim.answer_fadein);
            // アニメーション開始時間を解答欄ごとに少しずつずらす
            animFadeOut[i].setStartOffset(i * 75);
            animFadeIn[i].setStartOffset(i * 75);
        }
        // アニメーションをセット
        for (int i = 0; i < 4; i++) {
            // 解答欄をフェードアウト
            answerColumn[i].startAnimation(animFadeOut[i]);
            // 正解をセットしてフェードイン
            correctAnswerColumn[i].setText(correctAnswer[i]);
            correctAnswerColumn[i].startAnimation(animFadeIn[i]);
        }
    }

    // 正解(ランダムな4桁の数字)をセットする
    private void setCorrectAnswer() {
        Utility u = new Utility();
        // 一時的な配列に0～9までを格納し、シャッフルする
        String[] tmp = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
        u.shuffle(tmp);
        // シャッフルした配列から先頭4つを取り出して正解とする
        System.arraycopy(tmp, 0, correctAnswer, 0, 4);
        // 正解をログ出力
        Log.d("debug", "[hbdebug]" + "CorrectAnswer = " + correctAnswer[0]
                + correctAnswer[1] + correctAnswer[2] + correctAnswer[3]);
    }

    // 成績を表示する
    private void dispRecord() {
        // layoutInflater取得
        LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // 成績表示用のレイアウトをXMLから取得
        View layout = layoutInflater.inflate(R.layout.record_alert, null);

        // 表示内容を整備
        // ゲーム回数
        long gameCount = pref.getLong("gameCount", 0);
        // ゲーム中であれば、そのゲームはカウントしないようにするために1引いておく
        if (playing) {
            gameCount--;
        }
        // クリア回数
        long clearCount = pref.getLong("clearCount", 0);
        // 回答数の平均
        float answerAvg = pref.getFloat("answerAvg", -1);
        // 正解率
        float clearRatio = 0;
        if (gameCount >= 1) {
            clearRatio = ((float) clearCount / (float) gameCount) * 100;
        }

        // 表示内容をセット
        ((TextView) layout.findViewById(R.id.record_gameCount)).setText(""
                + gameCount);
        ((TextView) layout.findViewById(R.id.record_clearCount)).setText(""
                + clearCount);
        ((TextView) layout.findViewById(R.id.record_clearRatio)).setText(String
                .format("%.1f", clearRatio));
        if (answerAvg == -1) {
            // 一度もクリアしていない場合は平均なし(ハイフン)
            ((TextView) layout.findViewById(R.id.record_answerAvg))
                    .setText("-");
        } else {
            ((TextView) layout.findViewById(R.id.record_answerAvg))
                    .setText(String.format("%.1f", answerAvg));
        }

        // アラートを表示
        AlertDialog.Builder ab = new AlertDialog.Builder(me);
        ab.setTitle("成績");
        ab.setView(layout);
        ab.setPositiveButton("OK", null);
        ab.show();
    }
}