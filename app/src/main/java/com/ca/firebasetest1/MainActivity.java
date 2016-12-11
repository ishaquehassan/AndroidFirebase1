package com.ca.firebasetest1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public Firebase myFirebaseRef = null;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);
        myFirebaseRef = new Firebase("https://caandroidtest.firebaseio.com/messages/");

        mRecyclerView = (RecyclerView) findViewById(R.id.dataView);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new MyAdapter(myFirebaseRef, Item.class, myFirebaseRef);
        mRecyclerView.setAdapter(mAdapter);

        AuthData authData = myFirebaseRef.getAuth();
        if (authData != null) {
            Toast.makeText(getApplicationContext(),"Logged In",Toast.LENGTH_LONG).show();
        } else {
            myFirebaseRef.authWithOAuthToken("google", FirebaseInstanceId.getInstance().getToken(), new Firebase.AuthResultHandler() {
                @Override
                public void onAuthenticated(AuthData authData) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("provider", authData.getProvider());
                    if(authData.getProviderData().containsKey("displayName")) {
                        map.put("displayName", authData.getProviderData().get("displayName").toString());
                    }
                    myFirebaseRef.child("users").child(authData.getUid()).setValue(map);
                }

                @Override
                public void onAuthenticationError(FirebaseError firebaseError) {
                    Log.e("FireBase LoginError",firebaseError.getMessage());
                    switch (firebaseError.getCode()) {
                        case FirebaseError.USER_DOES_NOT_EXIST:
                            // handle a non existing user
                            break;
                        case FirebaseError.INVALID_PASSWORD:
                            // handle an invalid password
                            break;
                        default:
                            // handle other errors
                            break;
                    }
                }
            });
        }


        Button btn = (Button) findViewById(R.id.btn);
        final EditText et = (EditText) findViewById(R.id.name);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et.getText().length() > 0) {
                    et.setError(null);
                    Map<String, String> item = new HashMap<String, String>();
                    item.put("name", et.getText().toString());
                    myFirebaseRef.push().setValue(item);
                    et.setText("");
                } else {
                    et.setError("Please Fill Out Name!");
                }
            }
        });

    }


    public class MyAdapter extends FirebaseRecyclerAdapter<MyAdapter.ViewHolder, Item> {

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView textViewName;
            ImageView delBtn;

            ViewHolder(View view) {
                super(view);
                textViewName = (TextView) view.findViewById(R.id.nameTxt);
                delBtn = (ImageView) view.findViewById(R.id.del);
            }
        }

        MyAdapter(Query query, Class<Item> itemClass, Firebase dbRef) {
            super(query, itemClass, dbRef);
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, final int position) {
            Item item = getItem(position);
            holder.textViewName.setText(item.getName());
            final String key = getKeys().get(position);
            holder.delBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteItem(key);
                }
            });
        }

        @Override
        protected void itemAdded(Item item, String key, int position) {}

        @Override
        protected void itemChanged(Item oldItem, Item newItem, String key, int position) {}

        @Override
        protected void itemRemoved(Item item, String key, int position) {}

        @Override
        protected void itemMoved(Item item, String key, int oldPosition, int newPosition) {}
    }
}
