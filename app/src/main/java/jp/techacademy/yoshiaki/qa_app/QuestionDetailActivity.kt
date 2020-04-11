package jp.techacademy.yoshiaki.qa_app
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ListView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*

import java.util.HashMap
import android.content.SharedPreferences
import android.support.v4.app.SupportActivity
import android.support.v4.app.SupportActivity.ExtraData
import android.support.v4.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.R.attr.name



class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var favorit_type:String
    // Inflate the menu this adds items to the action bar if it is present.
    var favorite_type:String?="Add"
    var judge:Int=0
    val user_UID = FirebaseAuth.getInstance().currentUser
    val dataBaseReference = FirebaseDatabase.getInstance().reference

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }

    private val vEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value
            val favoriteUid = dataSnapshot.key ?: ""
            val favorite = map
            val sp = PreferenceManager.getDefaultSharedPreferences(this@QuestionDetailActivity)
            val editor = sp.edit()

            if(favorite == "Add" || favorite == null){
                favorite_type="Add"
                favorite_button.text="Add favorite"
            }else{
                favorite_type="Delete"
                favorite_button.text="Delete favorite"
            }
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            favorite_type="Add"
            favorite_button.text="Add favorite"

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        val sp = PreferenceManager.getDefaultSharedPreferences(this)
        val user = sp.getString(NameKEY, "")as String
        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                // --- ここから ---
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
                // --- ここまで ---
            }
        }

        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        var  favoriteRef=dataBaseReference.child(FavoritePATH).child(user_UID!!.uid).child(mQuestion.questionUid)//.child(mQuestion.genre.toString())
        favoriteRef.addChildEventListener(vEventListener)

        favorite_button.setOnClickListener(){ view ->
            clickaction(favorite_type.toString())

        }
    }

fun clickaction(favorite_type:String) {
    if (favorite_type == "Add") {
        var data= HashMap<String, String>()
        data["genre"] =mQuestion.genre.toString()

        var favoriteRef_add =
            dataBaseReference.child(FavoritePATH).child(user_UID!!.uid).child(mQuestion.questionUid)
        favoriteRef_add.setValue(data)



    } else {
        var favoriteRef_delete = dataBaseReference.child(FavoritePATH).child(user_UID!!.uid)
            .child(mQuestion.questionUid)//.child(mQuestion.genre.toString())
        favoriteRef_delete.removeValue()
    }
}



 }



