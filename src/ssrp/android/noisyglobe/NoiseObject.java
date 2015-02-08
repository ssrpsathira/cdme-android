package ssrp.android.noisyglobe;

public class NoiseObject {
	protected Double soundLevel;
	protected Double latitude;
	protected Double longitude;
	protected Integer dateTime;
	
	public NoiseObject(String[] param){
		this.soundLevel = Double.parseDouble(param[0]);
		this.longitude = Double.parseDouble(param[1]);
		this.latitude = Double.parseDouble(param[2]);
		this.dateTime = Integer.parseInt(param[3]);
	}
	
	public Double getSoundLevel() {
		return soundLevel;
	}
	public void setSoundLevel(Double soundLevel) {
		this.soundLevel = soundLevel;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Integer getDateTime() {
		return dateTime;
	}
	public void setDateTime(Integer dateTime) {
		this.dateTime = dateTime;
	}
	
}
