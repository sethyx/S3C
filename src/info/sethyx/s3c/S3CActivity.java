package info.sethyx.s3c;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class S3CActivity extends Activity {

	private static final String TAG = "S3C";

	private static final String s3MacAddr = "";
	private static final String s3IPAddr = "";
	private static final String s3StopCMD = "sleepykitty";
	private static final String s3CancelCMD = "oopsNOOO";
	private static final String s3XBMCCMD = "XBMC";
	private static final int s3Port = 9;

	private Button startButton;
	private Button stopButton;
	private Button cancelButton;
	private Button XBMCButton;

	private boolean sentSuccessfully;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	@Override
	public void onAttachedToWindow() {
		this.startButton = (Button) this.findViewById(R.id.start);
		this.stopButton = (Button) this.findViewById(R.id.stop);
		this.cancelButton = (Button) this.findViewById(R.id.cancel);
		this.XBMCButton = (Button) this.findViewById(R.id.xbmc);
		sentSuccessfully = false;

		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Thread networkThread = new Thread(new Runnable() {
					public void run() {
						sendWOL();
					}
				});
				networkThread.start();
				synchronized (networkThread) {
					try {
						networkThread.join();
						Toast.makeText(
								v.getContext(),
								"WOL packet "
										+ (sentSuccessfully ? "" : "NOT ")
										+ "sent to SethPC!", Toast.LENGTH_SHORT)
								.show();
						sentSuccessfully = false;
					} catch (InterruptedException e) {
						Log.e(TAG, "Network thread interrupted: " + e);
					}
				}
			}
		});

		stopButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Thread networkThread = new Thread(new Runnable() {
					public void run() {
						sendPacket(s3StopCMD.getBytes());
					}
				});
				networkThread.start();
				synchronized (networkThread) {
					try {
						networkThread.join();
						stopButton.setVisibility(View.GONE);
						cancelButton.setVisibility(View.VISIBLE);
						Toast.makeText(
								v.getContext(),
								"STOP packet "
										+ (sentSuccessfully ? "" : "NOT ")
										+ "sent to SethPC!", Toast.LENGTH_SHORT)
								.show();
						sentSuccessfully = false;
					} catch (InterruptedException e) {
						Log.e(TAG, "Network thread interrupted: " + e);
					}
				}
			}
		});

		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Thread networkThread = new Thread(new Runnable() {
					public void run() {
						sendPacket(s3CancelCMD.getBytes());
					}
				});
				networkThread.start();
				synchronized (networkThread) {
					try {
						networkThread.join();
						cancelButton.setVisibility(View.GONE);
						stopButton.setVisibility(View.VISIBLE);
						Toast.makeText(
								v.getContext(),
								"CANCEL packet "
										+ (sentSuccessfully ? "" : "NOT ")
										+ "sent to SethPC!", Toast.LENGTH_SHORT)
								.show();
						sentSuccessfully = false;
					} catch (InterruptedException e) {
						Log.e(TAG, "Network thread interrupted: " + e);
					}
				}
			}
		});
		
		XBMCButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Thread networkThread = new Thread(new Runnable() {
					public void run() {
						sendPacket(s3XBMCCMD.getBytes());
					}
				});
				networkThread.start();
				synchronized (networkThread) {
					try {
						networkThread.join();
						cancelButton.setVisibility(View.GONE);
						stopButton.setVisibility(View.VISIBLE);
						Toast.makeText(
								v.getContext(),
								"XBMC packet "
										+ (sentSuccessfully ? "" : "NOT ")
										+ "sent to SethPC!", Toast.LENGTH_SHORT)
								.show();
						sentSuccessfully = false;
					} catch (InterruptedException e) {
						Log.e(TAG, "Network thread interrupted: " + e);
					}
				}
			}
		});
	}

	private void sendWOL() {
		byte[] macBytes = getMacBytes(s3MacAddr);
		if (!Arrays.equals(macBytes, new byte[] { 0, 0, 0, 0, 0, 0 })) {
			byte[] bytes = new byte[6 + 16 * macBytes.length];
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) 0xff;
			}
			for (int i = 6; i < bytes.length; i += macBytes.length) {
				System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
			}

			sendPacket(bytes);
		}
	}

	private void sendPacket(byte[] bytes) {
		try {
			InetAddress address = InetAddress.getByName(s3IPAddr);
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
					address, s3Port);
			DatagramSocket socket = new DatagramSocket();
			socket.send(packet);
			socket.close();
			sentSuccessfully = true;
		} catch (Exception e) {
			Log.e(TAG, "Error while sending packet: " + e);
		}
	}

	private static byte[] getMacBytes(String macStr) {
		byte[] bytes = new byte[6];
		String[] hex = macStr.split("(\\:|\\-)");
		if (hex.length != 6) {
			Log.e(TAG, "Invalid MAC address.");
			return bytes;
		}
		try {
			for (int i = 0; i < 6; i++) {
				bytes[i] = (byte) Integer.parseInt(hex[i], 16);
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, "Invalid hex digit in MAC address.");
		}
		return bytes;
	}

}