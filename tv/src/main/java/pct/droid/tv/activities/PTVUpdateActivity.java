package pct.droid.tv.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import pct.droid.tv.R;
import pct.droid.tv.activities.base.PTVBaseActivity;

public class PTVUpdateActivity extends PTVBaseActivity {

	public static Intent startActivity(Context context) {
		Intent intent = new Intent(context, PTVUpdateActivity.class);
		context.startActivity(intent);
		return intent;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState,R.layout.activity_update);
	}

}
