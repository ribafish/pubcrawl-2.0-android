package com.ws1617.iosl.pubcrawl20;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.ws1617.iosl.pubcrawl20.Utilites.SignInHelper;


/**
 * Created by Icke-Hier on 08.02.2017.
 */
public class SignInActivity extends AppCompatActivity implements
	GoogleApiClient.OnConnectionFailedListener,
	View.OnClickListener {

	private static final String TAG = "SignInActivity";
	private static final int RC_SIGN_IN = 9008;

	private GoogleApiClient mGoogleApiClient;
	private ProgressDialog mProgressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign_in);

		// Button listeners
		findViewById(R.id.sign_in_button).setOnClickListener(this);

		// [START configure_signin]
		// Configure sign-in to request the user's ID, email address, and basic
		// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
		String serverClientId = getString(R.string.server_client_id);
		GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
			.requestServerAuthCode(serverClientId, false)
			.build();
		// [END configure_signin]

		// [START build_client]
		// Build a GoogleApiClient with access to the Google Sign-In API and the
		// options specified by gso.
		mGoogleApiClient = new GoogleApiClient.Builder(this)
			.enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
			.addApi(Auth.GOOGLE_SIGN_IN_API, gso)
			.build();
		// [END build_client]

		// [START customize_button]
		// Set the dimensions of the sign-in button.
		SignInButton signInButton = (SignInButton) findViewById(R.id.sign_in_button);
		signInButton.setSize(SignInButton.SIZE_STANDARD);
		// [END customize_button]
	}

	@Override
	public void onStart() {
		super.onStart();

		OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
		if (opr.isDone()) {
			// If the user's cached credentials are valid, the OptionalPendingResult will be "done"
			// and the GoogleSignInResult will be available instantly.
			Log.d(TAG, "Got cached sign-in");
			GoogleSignInResult result = opr.get();
			handleSignInResult(result);
		} else {
			// If the user has not previously signed in on this device or the sign-in has expired,
			// this asynchronous branch will attempt to sign in the user silently.  Cross-device
			// single sign-on will occur in this branch.
			showProgressDialog();
			opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
				@Override
				public void onResult(GoogleSignInResult googleSignInResult) {
					hideProgressDialog();
					handleSignInResult(googleSignInResult);
				}
			});
		}
	}

	// [START onActivityResult]
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
		if (requestCode == RC_SIGN_IN) {
			if (resultCode == RESULT_OK) {
				GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
				handleSignInResult(result);
			} else {
				Log.d(TAG, "Result from on activity result is not ok " + resultCode);
			}
		}
	}
	// [END onActivityResult]

	// [START handleSignInResult]
	private void handleSignInResult(GoogleSignInResult result) {
		Log.d(TAG, "handleSignInResult:" + result.isSuccess());
		if (result.isSuccess()) {
			// Signed in successfully, show authenticated UI.
			GoogleSignInAccount acct = result.getSignInAccount();
			SignInHelper signIn = new SignInHelper(this);
			signIn.getToken(acct);

		} else {
			// Signed out, show unauthenticated UI.
			Log.d(TAG, "Signed out - Shouldnt be here!");
		}
	}
	// [END handleSignInResult]

	// [START signIn]
	private void signIn() {
		Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
		startActivityForResult(signInIntent, RC_SIGN_IN);
	}
	// [END signIn]

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		// An unresolvable error has occurred and Google APIs (including Sign-In) will not
		// be available.
		Log.d(TAG, "onConnectionFailed:" + connectionResult);
	}

	private void showProgressDialog() {
		if (mProgressDialog == null) {
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setMessage(getString(R.string.loading));
			mProgressDialog.setIndeterminate(true);
		}

		mProgressDialog.show();
	}

	private void hideProgressDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing()) {
			mProgressDialog.hide();
		}
	}

	@Override
	protected void onDestroy() {
		try {
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		super.onDestroy();
	}

	public void showApp() {
		Intent intent = new Intent(this, MainActivity.class);
		this.startActivity(intent);
	}



	@Override
	protected void onPause() {
		super.onPause();

		if(mProgressDialog != null) {
			mProgressDialog.dismiss();
			mProgressDialog = null;
		}
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.sign_in_button)
			signIn();
	}
}