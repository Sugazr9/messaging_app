class GroupDetailActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupMemberAdapter
    private lateinit var group: Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_detail)

        // Get group data from intent
        group = intent.getSerializableExtra("GROUP") as Group

        recyclerView = findViewById(R.id.recyclerViewMembers)
        adapter = GroupMemberAdapter(this, group.members) { contact ->
            sendMessage(contact)
        }
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun sendMessage(contact: Contact) {
        val phoneNumber = contact.phoneNumber
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phoneNumber")
            // Optionally add a pre-filled message
            // putExtra("sms_body", "Your message here")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No messaging app found.", Toast.LENGTH_SHORT).show()
        }
    }
}
