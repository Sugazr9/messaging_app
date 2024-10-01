object PermissionsHelper {
    private const val REQUEST_READ_CONTACTS = 100

    fun hasReadContactsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestReadContactsPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.READ_CONTACTS),
            REQUEST_READ_CONTACTS
        )
    }

    // Handle the result in the activity's onRequestPermissionsResult
}
