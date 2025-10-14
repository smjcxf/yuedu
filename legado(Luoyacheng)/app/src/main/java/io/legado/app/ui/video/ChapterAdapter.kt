package io.legado.app.ui.video

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.legado.app.R
import io.legado.app.data.entities.BookChapter
import io.legado.app.lib.theme.ThemeStore.Companion.accentColor

class ChapterAdapter(
    private val chapters: List<BookChapter>,
    private var selectedPosition: Int = -1,
    private val onChapterClick: (BookChapter) -> Unit
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChapterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_chapter, parent, false)
        return ChapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(chapters[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = chapters.size

    inner class ChapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvChapterName: TextView = itemView.findViewById(R.id.tvChapterName)

        fun bind(chapter: BookChapter, isSelected: Boolean) {
            tvChapterName.text = chapter.title
            if (isSelected) {
                tvChapterName.setTextColor(accentColor)
            }
            else {
                tvChapterName.setTextColor(ContextCompat.getColor(itemView.context,R.color.primaryText))
            }
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = bindingAdapterPosition
                if (previousPosition >= 0) {
                    notifyItemChanged(previousPosition) //更新之前的
                }
                if (selectedPosition >= 0) {
                    notifyItemChanged(selectedPosition) //更新当前选中的
                }
                onChapterClick(chapter)
            }
        }
    }
}