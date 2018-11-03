package jp.gr.java_conf.mu.hitandblowgame;

class HistoryRowData {
	private int no = 0;
	private String answer = null;
	private int hit = 0;
	private int blow = 0;

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public int getHit() {
		return hit;
	}

	public void setHit(int hit) {
		this.hit = hit;
	}

	public int getBlow() {
		return blow;
	}

	public void setBlow(int blow) {
		this.blow = blow;
	}

	public boolean equals(HistoryRowData data){
		return data.answer.equals(this.answer);
	}
}