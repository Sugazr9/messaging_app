package com.example.massmessagingapp.adapters

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.massmessagingapp.R
import com.example.massmessagingapp.models.Group

class GroupAdapter(
    private val groupList: List<Group>,
    private val onGroupSelected: (Group) -> Unit,    // Callback for selecting a group
    private val onGroupAction: (Group, String) -> Unit  // Callback for Edit/Delete actions
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    private var selectedGroup: Group? = null  // Track the selected group

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupName: TextView = itemView.findViewById(R.id.group_name)
        private val groupContactsCount: TextView = itemView.findViewById(R.id.group_contacts_count)
        private var nameFont = ResourcesCompat.getFont(itemView.context, R.font.luckiest_guy)
        private var countFont = ResourcesCompat.getFont(itemView.context, R.font.fredoka_standard)

        fun bind(group: Group, isSelected: Boolean) {

            //resetting click listen as a bug fix
            itemView.setOnClickListener(null)
            groupName.text = group.name
            groupContactsCount.text = itemView.context.resources.getQuantityString(R.plurals.group_adapter_contact_count, group.contacts.size, group.contacts.size)

            // Change background and text bolding if the item is selected
            if (isSelected) {
                itemView.background = AppCompatResources.getDrawable(itemView.context, R.drawable.comic_background_item)
                groupName.setTypeface(nameFont, Typeface.BOLD)
                groupContactsCount.setTypeface(countFont, Typeface.BOLD)
            }
            else {
                itemView.setBackgroundColor(Color.TRANSPARENT)
                groupName.setTypeface(nameFont, Typeface.NORMAL)
                groupContactsCount.setTypeface(countFont, Typeface.NORMAL)
            }

            // Handle regular click to select the group
            itemView.setOnClickListener {
                onGroupSelected(group)
            }

            // Handle long click to show the options dialog (Edit/Delete)
            itemView.setOnLongClickListener {
                showGroupEditPopup(group, itemView)
                true  // Return true to indicate the long-click was handled
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return GroupViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val group = groupList[position]
        holder.bind(group, group == selectedGroup)  // Pass selected state
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    private fun showGroupEditPopup(group: Group, anchorView: View) {
        // Inflate the custom layout
        val inflater = LayoutInflater.from(anchorView.context)
        val popupView = inflater.inflate(R.layout.group_edit_dialog, null, false)

        // Initialize the PopupWindow with the custom layout
        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set click listeners for each button in the custom layout
        popupView.findViewById<ImageButton>(R.id.btn_edit).setOnClickListener {
            onGroupAction(group, "edit")
            popupWindow.dismiss() // Close the popup after clicking
        }

        popupView.findViewById<ImageButton>(R.id.btn_delete).setOnClickListener {
            onGroupAction(group, "delete")
            popupWindow.dismiss() // Close the popup after clicking
        }

        // Show the PopupWindow below the anchor view
        popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    fun updateSelectedGroup(group: Group?) {
        selectedGroup = group
    }
}