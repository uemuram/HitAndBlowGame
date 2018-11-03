package jp.gr.java_conf.mu.hitandblowgame;

class Utility {
	/**
	 * 配列の要素の重複チェックを行う
	 * 
	 * @param arr
	 *            チェック対象の配列
	 * @return 重複があれば重複箇所のインデックス(0以上)、なければ-1
	 */
	public int duplicateCheck(String[] arr) {
		// 重複箇所のインデックス
		int idx = -1;
		loop: for (int i = 0; i < arr.length - 1; i++) {
			for (int j = i + 1; j < arr.length; j++) {
				if (arr[i].equals(arr[j])) {
					idx = i;
					break loop;
				}
			}
		}
		return idx;
	}

	/**
	 * 配列のnullチェックを行う
	 * 
	 * @param arr
	 *            チェック対象の配列
	 * @return nullもしくは空文字があればそのインデックス(0以上)、なければ-1
	 */
	public int nullCheck(String[] arr) {
		// 重複箇所のインデックス
		int idx = -1;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].equals("") || arr[i] == null) {
				idx = i;
				break;
			}
		}
		return idx;
	}

	/**
	 * 配列の指定した要素を入れ替える
	 * 
	 * @param arr
	 *            対象の配列
	 * @param idx1
	 *            入れ替え対象のインデックス(1)
	 * @param idx2
	 *            入れ替え対象のインデックス(2)
	 */
	private void swap(String[] arr, int idx1, int idx2) {
		String tmp = arr[idx1];
		arr[idx1] = arr[idx2];
		arr[idx2] = tmp;
	}

	/**
	 * 配列をシャッフルする
	 * 
	 * @param arr
	 *            対象の配列
	 */
	public void shuffle(String[] arr) {
		// Fisher-Yates法でシャッフル
		int j;
		for (int i = arr.length - 1; i > 0; i--) {
			j = (int) (Math.random() * (i + 1));
			swap(arr, i, j);
		}
	}

}
