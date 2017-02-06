/*
TutorialChatRoom.java
Copyright (C) 2010  Belledonne Communications SARL 

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.antwork.core.tutorials;

import java.nio.ByteBuffer;

import org.antwork.core.LinphoneAddress;
import org.antwork.core.LinphoneCall;
import org.antwork.core.LinphoneCall.State;
import org.antwork.core.LinphoneCallStats;
import org.antwork.core.LinphoneChatMessage;
import org.antwork.core.LinphoneChatRoom;
import org.antwork.core.LinphoneContent;
import org.antwork.core.LinphoneCore;
import org.antwork.core.LinphoneCore.EcCalibratorStatus;
import org.antwork.core.LinphoneCore.GlobalState;
import org.antwork.core.LinphoneCore.LogCollectionUploadState;
import org.antwork.core.LinphoneCore.RegistrationState;
import org.antwork.core.LinphoneCore.RemoteProvisioningState;
import org.antwork.core.LinphoneAuthInfo;
import org.antwork.core.LinphoneCoreException;
import org.antwork.core.LinphoneCoreFactory;
import org.antwork.core.LinphoneCoreListener;
import org.antwork.core.LinphoneEvent;
import org.antwork.core.LinphoneFriend;
import org.antwork.core.LinphoneFriendList;
import org.antwork.core.LinphoneInfoMessage;
import org.antwork.core.LinphoneProxyConfig;
import org.antwork.core.PublishState;
import org.antwork.core.SubscriptionState;


/**
 * This program is a _very_ simple usage example of liblinphone.
 * It demonstrates how to send/receive SIP MESSAGE from a sip uri identity
 * passed from the command line.
 *
 * Argument must be like sip:jehan@sip.linphone.org .
 *
 * ex chatroom sip:jehan@sip.linphone.org
 * just takes a sip-uri as first argument and attempts to call it.
 * 
 * Ported from chatroom.c
 *
 * @author Guillaume Beraudo
 *
 */
public class TutorialChatRoom implements LinphoneCoreListener, LinphoneChatMessage.StateListener {
	private boolean running;
	private TutorialNotifier TutorialNotifier;


	public TutorialChatRoom(TutorialNotifier TutorialNotifier) {
		this.TutorialNotifier = TutorialNotifier;
	}

	public TutorialChatRoom() {
		this.TutorialNotifier = new TutorialNotifier();
	}

	
	
	public void show(LinphoneCore lc) {}
	public void byeReceived(LinphoneCore lc, String from) {}
	public void authInfoRequested(LinphoneCore lc, String realm, String username, String domain) {}
	public void authenticationRequested(LinphoneCore lc, LinphoneAuthInfo authInfo, LinphoneCore.AuthMethod method) {}
	public void displayStatus(LinphoneCore lc, String message) {}
	public void displayMessage(LinphoneCore lc, String message) {}
	public void displayWarning(LinphoneCore lc, String message) {}
	public void globalState(LinphoneCore lc, GlobalState state, String message) {}
	public void registrationState(LinphoneCore lc, LinphoneProxyConfig cfg,RegistrationState cstate, String smessage) {}
	public void newSubscriptionRequest(LinphoneCore lc, LinphoneFriend lf,String url) {}
	public void notifyPresenceReceived(LinphoneCore lc, LinphoneFriend lf) {}
	public void callState(LinphoneCore lc, LinphoneCall call, State cstate, String msg){}
	public void callStatsUpdated(LinphoneCore lc, LinphoneCall call, LinphoneCallStats stats) {}
	public void ecCalibrationStatus(LinphoneCore lc, EcCalibratorStatus status,int delay_ms, Object data) {}
	public void callEncryptionChanged(LinphoneCore lc, LinphoneCall call,boolean encrypted, String authenticationToken) {}
	public void notifyReceived(LinphoneCore lc, LinphoneCall call, LinphoneAddress from, byte[] event){}
	public void dtmfReceived(LinphoneCore lc, LinphoneCall call, int dtmf) {}

	
	
	public static void main(String[] args) {
		// Check tutorial was called with the right number of arguments
		// Takes the sip uri identity from the command line arguments
		if (args.length != 1) {
			throw new IllegalArgumentException("Bad number of arguments");
		}
		
		// Create tutorial object
		TutorialChatRoom tutorial = new TutorialChatRoom();
		try {
			String destinationSipAddress = args[1];
			tutorial.launchTutorial(destinationSipAddress);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	public void launchTutorial(String destinationSipAddress) throws LinphoneCoreException {
		
		// First instantiate the core Linphone object given only a listener.
		// The listener will react to events in Linphone core.
		LinphoneCore lc = LinphoneCoreFactory.instance().createLinphoneCore(this, null);

		try {
			// Next step is to create a chat room
			LinphoneChatRoom chatRoom = lc.getOrCreateChatRoom(destinationSipAddress);
			
			// Send message
			LinphoneChatMessage chatMessage = chatRoom.createLinphoneChatMessage("Hello world");
			chatRoom.sendMessage(chatMessage, this);
			
			// main loop for receiving notifications and doing background linphonecore work
			running = true;
			while (running) {
				lc.iterate();
				try{
					Thread.sleep(50);
				} catch(InterruptedException ie) {
					write("Interrupted!\nAborting");
					return;
				}
			}

		} finally {
			write("Shutting down...");
			// You need to destroy the LinphoneCore object when no longer used
			lc.destroy();
			write("Exited");
		}
	}


	public void stopMainLoop() {
		running=false;
	}

	
	private void write(String s) {
		TutorialNotifier.notify(s);
	}

	@Override
	public void messageReceived(LinphoneCore lc, LinphoneChatRoom cr,
			LinphoneChatMessage message) {
		write("Message [" + message.getText() + "] received from [" + message.getFrom().asString() + "]");
	}

	@Override
	public void onLinphoneChatMessageStateChanged(LinphoneChatMessage msg,
			org.antwork.core.LinphoneChatMessage.State state) {
		write("Sent message [" + msg.getText() + "] new state is " + state.toString());
	}

	@Override
	public void transferState(LinphoneCore lc, LinphoneCall call,
			State new_call_state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void infoReceived(LinphoneCore lc, LinphoneCall call, LinphoneInfoMessage info) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void subscriptionStateChanged(LinphoneCore lc, LinphoneEvent ev,
			SubscriptionState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyReceived(LinphoneCore lc, LinphoneEvent ev,
			String eventName, LinphoneContent content) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void publishStateChanged(LinphoneCore lc, LinphoneEvent ev,
			PublishState state) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void isComposingReceived(LinphoneCore lc, LinphoneChatRoom cr) {
		if (cr.isRemoteComposing())
			write("Remote is writing a message");
		else
			write("Remote has stop writing");
	}

	@Override
	public void configuringStatus(LinphoneCore lc,
			RemoteProvisioningState state, String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileTransferProgressIndication(LinphoneCore lc,
			LinphoneChatMessage message, LinphoneContent content, int progress) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void fileTransferRecv(LinphoneCore lc, LinphoneChatMessage message,
			LinphoneContent content, byte[] buffer, int size) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int fileTransferSend(LinphoneCore lc, LinphoneChatMessage message,
			LinphoneContent content, ByteBuffer buffer, int size) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void uploadProgressIndication(LinphoneCore lc, int offset, int total) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void uploadStateChanged(LinphoneCore lc,
			LogCollectionUploadState state, String info) {
		// TODO Auto-generated method stub
		
	}

	
        @Override
        public void friendListCreated(LinphoneCore lc, LinphoneFriendList list) {
                // TODO Auto-generated method stub

        }

        @Override
        public void friendListRemoved(LinphoneCore lc, LinphoneFriendList list) {
                // TODO Auto-generated method stub

        }

}
