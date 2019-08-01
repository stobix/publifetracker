package stobix.app.lifetracker

class ColorsMeta {

    companion object {
        @JvmStatic var colorsUsed = listOf(
                R.attr.colorPrimary,
                R.attr.colorPrimaryDark,
                R.attr.colorAccent,

                android.R.attr.textColorPrimary,
                android.R.attr.textColorSecondary,
                android.R.attr.textColorTertiary,
                android.R.attr.windowBackground,

                R.attr.table_header_text,
                R.attr.tableView_headerColor,

                R.attr.table_data_text,
                R.attr.table_data_row_even,
                R.attr.table_data_row_odd,

                R.attr.button_plus_color
        ).sorted()
    }

}