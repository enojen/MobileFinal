package com.example.database;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.database.models.FriendlyMessage;
import com.example.database.models.User;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class ChatActivity extends AppCompatActivity {
	public static final String MESSAGES_CHILD = "chat";

	private DatabaseReference mFirebaseDatabaseReference;
	private FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder> mFirebaseAdapter;

	private Button mSendButton;
	private RecyclerView mMessageRecyclerView;
	private LinearLayoutManager mLinearLayoutManager;
	private EditText mMessageEditText;
	private String mEmail = "Anonymous";
	private FloatingActionButton mHelpMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		mMessageEditText = findViewById(R.id.messageEditText);
		this.registerForContextMenu(mMessageEditText);
		mMessageRecyclerView = findViewById(R.id.messageRecyclerView);
		mLinearLayoutManager = new LinearLayoutManager(this);
		mLinearLayoutManager.setStackFromEnd(true);
		final String[] messages = {"Selam", "Naber", "İyi"};

		mHelpMessage = (FloatingActionButton) findViewById(R.id.help_message);
		mHelpMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				AlertDialog.Builder dialog = new AlertDialog.Builder(ChatActivity.this);
				dialog.setItems(messages, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int i) {
						String string = Arrays.asList(messages).get(i);
						mMessageEditText.setText(string);
					}
				});
				AlertDialog dialog2 = dialog.create();
				dialog2.show();


			}
		});

		// Initialize Firebase Auth
		FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
		FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
		mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
		mFirebaseDatabaseReference.child("users").child(mFirebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				User user = dataSnapshot.getValue(User.class);
				mEmail = user.email;
			}

			@Override
			public void onCancelled(DatabaseError databaseError) {}
		});

		Query query = mFirebaseDatabaseReference.child(MESSAGES_CHILD);
		FirebaseRecyclerOptions<FriendlyMessage> options = new FirebaseRecyclerOptions.Builder<FriendlyMessage>()
				.setQuery(query, FriendlyMessage.class)
				.build();

		mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage, MessageViewHolder>(options) {
			@Override
			protected void onBindViewHolder(MessageViewHolder viewHolder, int position, FriendlyMessage friendlyMessage) {
				if (friendlyMessage.getUsername().equals(mEmail)) {
					viewHolder.row.setGravity(Gravity.END);
				} else {
					viewHolder.row.setGravity(Gravity.START);
				}
				viewHolder.messageTextView.setText(friendlyMessage.getText());
				viewHolder.messengerTextView.setText(friendlyMessage.getUsername());
			}

			@Override
			public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
				LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
				return new MessageViewHolder(inflater.inflate(R.layout.item_message, viewGroup, false));
			}
		};

		mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				super.onItemRangeInserted(positionStart, itemCount);
				int friendlyMessageCount = mFirebaseAdapter.getItemCount();
				int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
				// If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
				// to the bottom of the list to show the newly added message.
				if (lastVisiblePosition == -1 || (positionStart >= (friendlyMessageCount - 1) && lastVisiblePosition == (positionStart - 1))) {
					mMessageRecyclerView.scrollToPosition(positionStart);
				}
			}
		});
		mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
		mMessageRecyclerView.setAdapter(mFirebaseAdapter);

		mMessageEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				if (charSequence.toString().trim().length() > 0) {
					mSendButton.setEnabled(true);
				} else {
					mSendButton.setEnabled(false);
				}
			}

			@Override
			public void afterTextChanged(Editable editable) {
			}
		});

		mSendButton = findViewById(R.id.sendButton);
		mSendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mEmail);
				mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessage);
				mMessageEditText.setText("");
			}
		});
	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

		switch (v.getId()) {
			case R.id.messageEditText:
				this.getMenuInflater().inflate(R.menu.messages, menu);
				break;
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.m1:
				mMessageEditText.setText(getStringXML(R.string.message1));
				break;
			case R.id.m2:
				mMessageEditText.setText(getStringXML(R.string.message2));
				break;
			case R.id.m3:
				mMessageEditText.setText(getStringXML(R.string.message3));
				break;
			case R.id.m4:
				mMessageEditText.setText(getStringXML(R.string.message4));
				break;

		}
		return super.onContextItemSelected(item);
	}

	public String getStringXML(int id) {
		String x = this.getString(id);
		return x;
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mFirebaseAdapter != null) {
			mFirebaseAdapter.startListening();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mFirebaseAdapter != null) {
			mFirebaseAdapter.stopListening();
		}
	}

	public static class MessageViewHolder extends RecyclerView.ViewHolder {
		LinearLayout row;
		TextView messageTextView;
		TextView messengerTextView;

		MessageViewHolder(View v) {
			super(v);
			row = itemView.findViewById(R.id.row);
			messageTextView = itemView.findViewById(R.id.messageTextView);
			messengerTextView = itemView.findViewById(R.id.messengerTextView);
		}
	}
}