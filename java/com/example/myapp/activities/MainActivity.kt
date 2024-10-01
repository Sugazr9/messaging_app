class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GroupListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewGroups)
        adapter = GroupListAdapter(this, groupList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, CreateGroupActivity::class.java)
            startActivity(intent)
        }
    }
}
