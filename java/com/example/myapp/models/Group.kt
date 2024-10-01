// Example of Group entity
@Entity
data class Group(
    @PrimaryKey val groupId: String,
    val groupName: String,
    val memberIds: List<String>
)
