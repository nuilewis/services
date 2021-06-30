/*
 * Copyright (C) 2016 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.services.sync.actions.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.opendatakit.logging.WebLogger;
import org.opendatakit.properties.CommonToolProperties;
import org.opendatakit.properties.PropertiesSingleton;
import org.opendatakit.services.R;
import org.opendatakit.services.preferences.activities.IOdkAppPropertiesActivity;
import org.opendatakit.services.sync.actions.VerifyServerSettingsActions;
import org.opendatakit.services.sync.actions.activities.DoSyncActionCallback;
import org.opendatakit.services.sync.actions.activities.ISyncServiceInterfaceActivity;
import org.opendatakit.services.sync.actions.activities.VerifyServerSettingsActivity;
import org.opendatakit.services.utilities.UserState;
import org.opendatakit.sync.service.IOdkSyncServiceInterface;
import org.opendatakit.sync.service.SyncOverallResult;
import org.opendatakit.sync.service.SyncProgressEvent;
import org.opendatakit.sync.service.SyncProgressState;
import org.opendatakit.sync.service.SyncStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mitchellsundt@gmail.com
 */
public class VerifyServerSettingsFragment extends AbsSyncUIFragment {

  /**
   * Class handling actions corresponding to Button Clicks
   */
  private class OnButtonClick implements View.OnClickListener{

    @Override
    public void onClick(View v) {
      if(v.getId()==R.id.btnStartVerifyServer){
        onStartVerifyServerClick();
      }
      else if(v.getId()==R.id.btnStartVerifyUser) {
        onStartVerifyUserClick();
      }
    }
  }

  private static final String TAG = "VerifyServerSettingsFragment";

  public static final String NAME = "VerifyServerSettingsFragment";
  public static final int ID = R.layout.verify_server_settings_launch_fragment;

  private static final String VERIFY_SERVER_SETTINGS_ACTION = "verifyServerSettingsAction";

  private static final String PROGRESS_DIALOG_TAG = "progressDialogVerifySvr";
  private static final String OUTCOME_DIALOG_TAG = "outcomeDialogVerifySvr";

  private TextView tvHeading ,tvServerUrl, tvServerVerifyStatus, tvServerAnonymousStatus, tvServerLastVerifyTime,
          tvUsernameLabel, tvUsername, tvVerifyStatusLabel, tvVerifyStatus, tvLastSyncLabel, tvLastSync;

  private Button btnVerifyServer, btnVerifyUser;
  private PropertiesSingleton props;
  private UserState userState;

  private VerifyServerSettingsActions verifyServerSettingsAction = VerifyServerSettingsActions.IDLE;

  public VerifyServerSettingsFragment() {
    super(OUTCOME_DIALOG_TAG, PROGRESS_DIALOG_TAG);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(VERIFY_SERVER_SETTINGS_ACTION, verifyServerSettingsAction.name());
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    Intent incomingIntent = getActivity().getIntent();
    if (savedInstanceState != null && savedInstanceState
        .containsKey(VERIFY_SERVER_SETTINGS_ACTION)) {
      String action = savedInstanceState.getString(VERIFY_SERVER_SETTINGS_ACTION);
      try {
        verifyServerSettingsAction = VerifyServerSettingsActions.valueOf(action);
      } catch (IllegalArgumentException e) {
        verifyServerSettingsAction = VerifyServerSettingsActions.IDLE;
      }
    }
    disableButtons();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);

    View view = inflater.inflate(ID, container, false);

    if (savedInstanceState != null && savedInstanceState.containsKey(VERIFY_SERVER_SETTINGS_ACTION)) {
      String action = savedInstanceState.getString(VERIFY_SERVER_SETTINGS_ACTION);
      try {
        verifyServerSettingsAction = VerifyServerSettingsActions.valueOf(action);
      } catch (IllegalArgumentException e) {
        verifyServerSettingsAction = VerifyServerSettingsActions.IDLE;
      }
    }

    return view;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    findViewsAndAttachListeners(view);
  }

  @Override
  public void onResume() {
    super.onResume();
    updateUserInterface();
  }

  /**
   * Finding the different views required and attaching onClick Listeners to them
   */
  private void findViewsAndAttachListeners(View view){
    tvHeading=view.findViewById(R.id.tvUserHeadingVerifySettings);
    tvServerUrl=view.findViewById(R.id.tvServerUrlVerify);
    tvServerVerifyStatus=view.findViewById(R.id.tvServerVerifyStatusVerify);
    tvServerAnonymousStatus=view.findViewById(R.id.tvServerAnonymousAllowedVerify);
    tvServerLastVerifyTime=view.findViewById(R.id.tvServerLastVerifyVerify);
    tvUsernameLabel=view.findViewById(R.id.tvUsernameLabelVerify);
    tvUsername=view.findViewById(R.id.tvUsernameVerify);
    tvVerifyStatusLabel=view.findViewById(R.id.tvVerificationStatusLabelVerify);
    tvVerifyStatus=view.findViewById(R.id.tvVerificationStatusVerify);
    tvLastSyncLabel=view.findViewById(R.id.tvLastSyncTimeLabelVerify);
    tvLastSync=view.findViewById(R.id.tvLastSyncTimeVerify);

    btnVerifyServer=view.findViewById(R.id.btnStartVerifyServer);
    btnVerifyUser=view.findViewById(R.id.btnStartVerifyUser);

    OnButtonClick onButtonClick=new OnButtonClick();
    btnVerifyUser.setOnClickListener(onButtonClick);
    btnVerifyServer.setOnClickListener(onButtonClick);
  }

  private void onStartVerifyServerClick(){

  }

  private void onStartVerifyUserClick(){
    WebLogger.getLogger(getAppName()).d(TAG,
            "[" + getId() + "] [onClickVerifyServerSettings] timestamp: " + System.currentTimeMillis());
    if (areCredentialsConfigured(true)) {
      disableButtons();
      verifyServerSettingsAction = VerifyServerSettingsActions.VERIFY;
      prepareForSyncAction();
    }
  }

  private void updateUserInterface(){
    props = ((IOdkAppPropertiesActivity) this.getActivity()).getProps();
    userState = UserState.valueOf(props.getProperty(CommonToolProperties.KEY_CURRENT_USER_STATE));

    updateCommonInfo();

    if (userState == UserState.LOGGED_OUT) {
      inLoggedOutState();
    } else if (userState == UserState.ANONYMOUS) {
      inAnonymousState();
    } else {
      inAuthenticatedState();
    }

  }

  private void updateCommonInfo(){
    String serverUrl=props.getProperty(CommonToolProperties.KEY_SYNC_SERVER_URL);
    tvServerUrl.setText(serverUrl);
    tvServerUrl.setPaintFlags(tvServerUrl.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

    boolean isServerVerified=Boolean.parseBoolean(props.getProperty(CommonToolProperties.KEY_IS_SERVER_VERIFIED));
    if(!isServerVerified){
      tvServerVerifyStatus.setText("Not Verified");
    }
    else {
      tvServerVerifyStatus.setText("Verified");
    }

    boolean isAnonymousLoginUsed=Boolean.parseBoolean(props.getProperty(CommonToolProperties.KEY_IS_ANONYMOUS_SIGN_IN_USED));
    if(isAnonymousLoginUsed){
      boolean isAnonymousAllowed=Boolean.parseBoolean(props.getProperty(CommonToolProperties.KEY_IS_ANONYMOUS_ALLOWED));
      if(isAnonymousAllowed){
        tvServerAnonymousStatus.setText("Allowed");
      }
      else {
        tvServerAnonymousStatus.setText("Not Allowed");
      }
    }
    else {
      tvServerAnonymousStatus.setText("Not Known Yet");
    }

    String lastServerVerifyTime=props.getProperty(CommonToolProperties.KEY_LAST_SERVER_VERIFIED_TIME);
    if(lastServerVerifyTime==null){
      tvServerLastVerifyTime.setText("Not Available");
    }
    else {
      tvServerLastVerifyTime.setText(lastServerVerifyTime);
    }
  }

  private void inLoggedOutState(){
    handleViewVisibility(View.VISIBLE,View.GONE);
    tvHeading.setText("User is Logged Out");
  }

  private void inAnonymousState(){
    handleViewVisibility(View.VISIBLE,View.GONE);
    tvHeading.setText("User is Anonymous");
  }

  private void inAuthenticatedState(){
    handleViewVisibility(View.GONE,View.VISIBLE);
    String username=props.getProperty(CommonToolProperties.KEY_USERNAME);
    tvUsername.setText(username);

    boolean userVerifyStatus=Boolean.parseBoolean(props.getProperty(CommonToolProperties.KEY_IS_USER_AUTHENTICATED));
    if(!userVerifyStatus){
      tvVerifyStatus.setText("Not Verified");
    }
    else {
      tvVerifyStatus.setText("Verified");
    }

    String lastSyncTime=props.getProperty(CommonToolProperties.KEY_LAST_SYNC_INFO);
    if(lastSyncTime==null){
      tvLastSync.setText("Not Available");
    }
    else {
      tvLastSync.setText(lastSyncTime);
    }
  }

  private void handleViewVisibility(int headingVisible, int userDetailVisible){
    tvHeading.setVisibility(headingVisible);

    tvUsernameLabel.setVisibility(userDetailVisible);
    tvUsername.setVisibility(userDetailVisible);
    tvVerifyStatusLabel.setVisibility(userDetailVisible);
    tvVerifyStatus.setVisibility(userDetailVisible);
    tvLastSyncLabel.setVisibility(userDetailVisible);
    tvLastSync.setVisibility(userDetailVisible);
    btnVerifyUser.setVisibility(userDetailVisible);
  }

  private void disableButtons() {
    btnVerifyUser.setEnabled(false);
    btnVerifyServer.setEnabled(false);
  }

  void perhapsEnableButtons() {
    PropertiesSingleton props = ((IOdkAppPropertiesActivity) this.getActivity()).getProps();
    String url = props.getProperty(CommonToolProperties.KEY_SYNC_SERVER_URL);
    if (url == null || url.length() == 0) {
      disableButtons();
    } else {
      btnVerifyUser.setEnabled(true);
      btnVerifyServer.setEnabled(true);
    }
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == VerifyServerSettingsActivity.AUTHORIZE_ACCOUNT_RESULT_CODE) {
      if (resultCode == Activity.RESULT_CANCELED) {
        verifyServerSettingsAction = VerifyServerSettingsActions.IDLE;
      }
      postTaskToAccessSyncService();
    }
  }

  void postTaskToAccessSyncService() {
    WebLogger.getLogger(getAppName()).d(TAG, "[" + getId() + "] [postTaskToAccessSyncService] started");
    Activity activity = getActivity();
    if (activity == null || !hasDialogBeenCreated() || !this.isResumed()) {
      // we are in transition -- do nothing
      WebLogger.getLogger(getAppName())
          .d(TAG, "[" + getId() + "] [postTaskToAccessSyncService] activity == null");
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          postTaskToAccessSyncService();
        }
      }, 100);

      return;
    }
    ((ISyncServiceInterfaceActivity) activity)
        .invokeSyncInterfaceAction(new DoSyncActionCallback() {
          @Override
          public void doAction(IOdkSyncServiceInterface syncServiceInterface)
              throws RemoteException {
            if (syncServiceInterface != null) {
              //          WebLogger.getLogger(getAppName()).d(TAG, "[" + getId() + "] [postTaskToAccessSyncService] syncServiceInterface != null");
              final SyncStatus status = syncServiceInterface.getSyncStatus(getAppName());
              final SyncProgressEvent event = syncServiceInterface
                  .getSyncProgressEvent(getAppName());
              if (status == SyncStatus.SYNCING) {
                verifyServerSettingsAction = VerifyServerSettingsActions.MONITOR_VERIFYING;

                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    showProgressDialog(status, event.progressState, event.progressMessageText,
                        event.curProgressBar, event.maxProgressBar);
                  }
                });
                return;
              }

              switch (verifyServerSettingsAction) {
              case VERIFY:
                syncServiceInterface.verifyServerSettings(getAppName());
                verifyServerSettingsAction = VerifyServerSettingsActions.MONITOR_VERIFYING;

                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    showProgressDialog(SyncStatus.NONE, null,
                        getString(R.string.verify_server_settings_starting), -1, 0);
                  }
                });
                break;
              case IDLE:
                if (event.progressState == SyncProgressState.FINISHED) {
                  final SyncOverallResult result = syncServiceInterface.getSyncResult(getAppName());
                  handler.post(new Runnable() {
                    @Override
                    public void run() {
                      showOutcomeDialog(status, result);
                    }
                  });
                }
              default:
                break;
              }
            } else {
              WebLogger.getLogger(getAppName())
                  .d(TAG, "[" + getId() + "] [postTaskToAccessSyncService] syncServiceInterface == null");
              // The service is not bound yet so now we need to try again
              handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                  postTaskToAccessSyncService();
                }
              }, 100);
            }
          }
        });
  }

  void updateInterface() {
    Activity activity = getActivity();
    if (activity == null || !hasDialogBeenCreated() || !this.isResumed()) {
      // we are in transition -- do nothing
      WebLogger.getLogger(getAppName())
          .w(TAG, "[" + getId() + "] [updateInterface] activity == null = return");
      handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          updateInterface();
        }
      }, 100);
      return;
    }
    ((ISyncServiceInterfaceActivity) activity)
        .invokeSyncInterfaceAction(new DoSyncActionCallback() {
          @Override
          public void doAction(IOdkSyncServiceInterface syncServiceInterface)
              throws RemoteException {
            if (syncServiceInterface != null) {
              final SyncStatus status = syncServiceInterface.getSyncStatus(getAppName());
              final SyncProgressEvent event = syncServiceInterface
                  .getSyncProgressEvent(getAppName());
              if (status == SyncStatus.SYNCING) {
                verifyServerSettingsAction = VerifyServerSettingsActions.MONITOR_VERIFYING;

                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    showProgressDialog(status, event.progressState, event.progressMessageText,
                        event.curProgressBar, event.maxProgressBar);
                  }
                });
                return;
              } else {
                // request completed
                verifyServerSettingsAction = VerifyServerSettingsActions.IDLE;
                final SyncOverallResult result = syncServiceInterface.getSyncResult(getAppName());
                handler.post(new Runnable() {
                  @Override
                  public void run() {
                    if (event.progressState == SyncProgressState.FINISHED) {
                      showOutcomeDialog(status, result);
                    }
                  }
                });
                return;
              }
            } else {
              // The service is not bound yet so now we need to try again
              handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                  updateInterface();
                }
              }, 100);
            }
          }
        });
  }

  void syncCompletedAction(IOdkSyncServiceInterface syncServiceInterface) throws
      RemoteException {
    removeAnySyncNotification();
    boolean completed = syncServiceInterface.clearAppSynchronizer(getAppName());
    if (!completed) {
      throw new IllegalStateException(
          "Could not remove AppSynchronizer for " + getAppName());
    }
    getActivity().finish();
  }

  private void showProgressDialog(SyncStatus status, SyncProgressState progress, String message,
      int progressStep, int maxStep) {
    if (getActivity() == null) {
      // we are tearing down or still initializing
      return;
    }
    if (verifyServerSettingsAction == VerifyServerSettingsActions.MONITOR_VERIFYING) {

      disableButtons();

      if (progress == null) {
        progress = SyncProgressState.INACTIVE;
      }

      int id_title = R.string.verifying_server_settings;
      showProgressDialog(getString(id_title), message, progressStep, maxStep);

      if (status == SyncStatus.SYNCING || status == SyncStatus.NONE) {
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            updateInterface();
          }
        }, 150);
      }
    }
  }

  private void showOutcomeDialog(SyncStatus status, SyncOverallResult result) {
    if (getActivity() == null) {
      // we are tearing down or still initializing
      return;
    }
    if (verifyServerSettingsAction == VerifyServerSettingsActions.IDLE) {

      disableButtons();

      String message;
      int id_title;
      switch (status) {
      default:
        throw new IllegalStateException("Unexpected missing case statement");
      case
          /** earlier sync ended with socket or lower level transport or protocol error (e.g., 300's) */ NETWORK_TRANSPORT_ERROR:
        id_title = R.string.sync_communications_error;
        message = getString(R.string.sync_status_network_transport_error);
        break;
      case
          /** earlier sync ended with Authorization denied (authentication and/or access) error */ AUTHENTICATION_ERROR:
        id_title = R.string.sync_user_authorization_failure;
        message = getString(R.string.sync_status_authentication_error);
        updatePropertiesOnVerifyComplete("Authentication Error");
        break;
      case
          /** earlier sync ended with a 500 error from server */ SERVER_INTERNAL_ERROR:
        id_title = R.string.sync_communications_error;
        message = getString(R.string.sync_status_internal_server_error);
        break;
      case /** the server is not an ODK Server - bad client config */ SERVER_IS_NOT_ODK_SERVER:
        id_title = R.string.sync_device_configuration_failure;
        message = getString(R.string.sync_status_bad_gateway_or_client_config);
        break;
      case
          /** earlier sync ended with a 400 error that wasn't Authorization denied */ REQUEST_OR_PROTOCOL_ERROR:
        id_title = R.string.sync_communications_error;
        message = getString(R.string.sync_status_request_or_protocol_error);
        break;
      case /** no earlier sync and no active sync */ NONE:
      case /** active sync -- get SyncProgressEvent to see current status */ SYNCING:
      case
          /** earlier sync ended successfully without conflicts but needs row-level attachments sync'd */ SYNC_COMPLETE_PENDING_ATTACHMENTS:
      case
          /** the server does not have any configuration, or no configuration for this client version */ SERVER_MISSING_CONFIG_FILES:
      case
          /** the device does not have any configuration to push to server */ SERVER_RESET_FAILED_DEVICE_HAS_NO_CONFIG_FILES:
      case
          /** while a sync was in progress, another device reset the app config, requiring a restart of
           * our sync */ RESYNC_BECAUSE_CONFIG_HAS_BEEN_RESET_ERROR:
      case
          /** earlier sync ended with one or more tables containing row conflicts or checkpoint rows */ CONFLICT_RESOLUTION:
      case
          /** error accessing or updating database */ DEVICE_ERROR:
        id_title = R.string.sync_device_internal_error;
        message = getString(R.string.sync_status_device_internal_error);
        break;
      case
          /** the server is not configured for this appName -- Site Admin / Preferences */ APPNAME_NOT_SUPPORTED_BY_SERVER:
        id_title = R.string.sync_server_configuration_failure;
        message = getString(R.string.sync_status_appname_not_supported_by_server);
        break;
      case
          /** earlier sync ended successfully without conflicts and all row-level attachments sync'd */ SYNC_COMPLETE:
        id_title = R.string.verify_server_setttings_successful;
        message = getString(R.string.verify_server_setttings_successful_text);
        updatePropertiesOnVerifyComplete("Successful");
        break;
      }
      createAlertDialog(getString(id_title), message);
    }
  }

  private void updatePropertiesOnVerifyComplete(String status){
    Map<String,String> properties = new HashMap<String,String>();
    if(status.equals("Authentication Error")){
      properties.put(CommonToolProperties.KEY_IS_SERVER_VERIFIED,"true");
      properties.put(CommonToolProperties.KEY_LAST_SERVER_VERIFIED_TIME,Long.toString(new Date().getTime()));
      properties.put(CommonToolProperties.KEY_IS_USER_AUTHENTICATED,"false");
    }
    else if(status.equals("Successful")) {
      properties.put(CommonToolProperties.KEY_IS_SERVER_VERIFIED,"true");
      properties.put(CommonToolProperties.KEY_LAST_SERVER_VERIFIED_TIME,Long.toString(new Date().getTime()));
      properties.put(CommonToolProperties.KEY_IS_USER_AUTHENTICATED,"true");
    }
    props.setProperties(properties);
    updateUserInterface();
  }
}
