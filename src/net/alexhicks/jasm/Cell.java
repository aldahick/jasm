package net.alexhicks.jasm;

public class Cell {
	public boolean isNumber = false;
	public String data = "";
	public int value = 0x0;
	public int position = 0;
	public String label = "";
	private String rawData;
	public Cell(String data, int position) {
		this.rawData = data;
		if (data.contains(":")) {
			this.label = data.split(":")[0];
			data = data.split(":")[1];
		}
		data = data.trim();
		try {
			this.value = Integer.parseInt(data);
			this.isNumber = true;
		} catch (NumberFormatException ex) {
			this.data = data;
		}
		this.position = position;
	}
	public Cell copy() {
		return new Cell(rawData, position);
	}
}
