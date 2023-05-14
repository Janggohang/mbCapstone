package com.example.gonggu.ui.post

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gonggu.databinding.FragmentBuyBinding
import com.example.gonggu.databinding.FragmentChatBinding
import com.example.gonggu.databinding.ItemChatListBinding
import com.example.gonggu.databinding.ItemPostListBinding
import com.example.gonggu.ui.chat.RecyclerDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//data class User (val profile :String, val name : String, val phonenumber : String, val email : String)
data class PostData (
    val content: String,
    val numOfPeople: Int,
    val price : Int,
    val title: String,
    val time: String,
    val uid: String
    )
{
    constructor(): this("",0,0,"","","")
}

class BuyFragment : Fragment() {// FirebaseAuth와 Firebase Realtime Database 객체 선언
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    // RecyclerView에 사용할 어댑터 객체와 데이터를 담을 ArrayList 선언
    private lateinit var mAdapter: PostAdapter
    private val PostList: ArrayList<PostData> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // 레이아웃 파일을 inflate하고 뷰 바인딩 객체를 생성
        val binding = FragmentBuyBinding.inflate(inflater, container, false)

        // FirebaseAuth와 Firebase Realtime Database 객체 초기화
        mAuth = Firebase.auth
        mDatabase = Firebase.database.reference.child("post")

        // RecyclerView에 사용할 어댑터를 초기화
        mAdapter = PostAdapter(PostList)

        // RecyclerView 설정

        binding.recyclerViewPostlist.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter }


        // Firebase Realtime Database에서 데이터를 가져와서 RecyclerView에 표시
        mDatabase.orderByChild("time").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newPostList: ArrayList<PostData> = ArrayList()

                for (postSnapshot in snapshot.children) {
                    val post = postSnapshot.getValue(PostData::class.java)
                    newPostList.add(0, post!!)
                }

                // 기존 리스트에 새로운 게시글 리스트를 맨 앞에 추가
                PostList.clear()
                PostList.addAll(newPostList)

                mAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // 실패 시 처리할 작업을 구현
            }
        })
        val spaceDecoration = RecyclerDecoration(40)
        binding.recyclerViewPostlist.addItemDecoration(spaceDecoration)


        return binding.root
    }

    private inner class PostAdapter(private val postList: ArrayList<PostData>) :
        RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ItemPostListBinding.inflate(inflater, parent, false)
            return PostViewHolder(binding)
        }

        override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
            val postData = postList[position]
            holder.bind(postData)

//            holder.itemView.setOnClickListener{
//                val intent = Intent(context, ChatActivity::class.java)
//
//                intent.putExtra("name",chatData.name)
//                intent.putExtra("uId",chatData.uid)
//
//                context?.startActivity(intent)
//            }
        }

        override fun getItemCount() = postList.size


        inner class PostViewHolder(private val binding: ItemPostListBinding) :
            RecyclerView.ViewHolder(binding.root) {

            private val now = Calendar.getInstance()
            private val postDataFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA)

            fun bind(postData: PostData) {
                binding.itemPostTitle.text = postData.title
                binding.itemPostPre.text = postData.content

                // 게시글 작성 시간 데이터 파싱
                val postTime = Calendar.getInstance().apply {
                    time = postDataFormat.parse(postData.time)!!
                }

                // 날짜, 시간 변환
                val diff = now.timeInMillis - postTime.timeInMillis
                val timeString = when {
                    diff < 60 * 1000 -> "방금 전"
                    diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}분 전"
                    postTime.get(Calendar.YEAR) == now.get(Calendar.YEAR) -> {
                        when (postTime.get(Calendar.DAY_OF_YEAR)) {
                            now.get(Calendar.DAY_OF_YEAR) -> postDataFormat.format(postTime.time).substring(11)
                            else -> SimpleDateFormat("MM/dd", Locale.KOREA).format(postTime.time)
                        }
                    }
                    else -> SimpleDateFormat("yy/MM/dd", Locale.KOREA).format(postTime.time)
                }
                binding.itemPostListTime.text = timeString
            }
        }
    }
}
