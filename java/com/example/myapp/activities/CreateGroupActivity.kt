class CreateGroupActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactListAdapter
    private val selectedContacts = mutableListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        // Check for permissions
        if (!PermissionsHelper.hasReadContactsPermission(this)) {
            PermissionsHelper.requestReadContactsPermission(this)
        } else {
            loadContacts()
        }

        val btnSaveGroup: Button = findViewById(R.id.btnSaveGroup)
        btnSaveGroup.setOnClickListener {
            saveGroup()
        }
    }

    private fun loadContacts() {
        val contacts = ContactsHelper.getContacts(this)
        recyclerView = findViewById(R.id.recyclerViewContacts)
        adapter = ContactListAdapter(this, contacts, selectedContacts)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun saveGroup() {
        // Implement group saving logic (e.g., add to a database or in-memory list)
    }
}
