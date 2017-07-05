package de.bzus.flame.ui;

import java.awt.Color;

import javax.swing.JLabel;

import de.bzus.flame.common.Logging;

public class MsgHandler {
	private static JLabel lblMessage = null;
	private static MsgHandler msgHdl = null;
	private String actualMessage = "";
	private String lastMessage = "";

	public static MsgHandler getMsgHdl(JLabel msg) {

		if (msgHdl == null)
			msgHdl = new MsgHandler(msg);
		else if (msg != null)
			lblMessage = msg;

		return msgHdl;
	}

	public static MsgHandler getMsgHdl() {
		if (msgHdl != null)
			return msgHdl;
		return getMsgHdl(null);
	}

	private MsgHandler(JLabel msg) {
		lblMessage = msg;
		msgHdl = this;
	}

	public void showError(String msg, int severity) {

		Logging.getErrorLog().showError(msg, severity);
		lastMessage = new String(actualMessage);
		actualMessage = new String(msg);

		StringBuffer sevText = new StringBuffer();
		Color sevCol = Color.BLACK;
		switch (severity) {
		case 0:
			sevText.append("(Information) ");
			sevCol = Color.BLUE;
			break;

		case 1:
			sevText.append("(Warning) ");
			sevCol = new Color(255, 127, 0);
			break;

		case 2:
			sevText.append("(Error) ");
			sevCol = Color.RED;
			break;

		case 3:
			sevText.append("(Panic) ");
			sevCol = Color.PINK;
			break;

		default:
			break;
		}
		sevText.append(msg);
		if (lblMessage != null) {
			lblMessage.setText(sevText.toString());
			lblMessage.setForeground(sevCol);
		}
	}

	public String getActualMessage() {
		return actualMessage;
	}

	public String getLastMessage() {
		return lastMessage;
	}

}
