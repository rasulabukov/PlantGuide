package com.example.plantguide

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.plantguide.databinding.QuestionItemBinding

class TestAdapter(
    private val questions: List<Question>,
    private val userAnswers: MutableList<Int>,
    private val onAnswerSelected: (Int, Int) -> Unit
) : RecyclerView.Adapter<TestAdapter.QuestionViewHolder>() {

    inner class QuestionViewHolder(val binding: QuestionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(question: Question, position: Int) {
            // Устанавливаем текст вопроса
            binding.tvQuestion.text = question.text

            // Устанавливаем варианты ответов
            (binding.radioGroup.getChildAt(0) as RadioButton).text = question.answers[0]
            (binding.radioGroup.getChildAt(1) as RadioButton).text = question.answers[1]
            (binding.radioGroup.getChildAt(2) as RadioButton).text = question.answers[2]
            (binding.radioGroup.getChildAt(3) as RadioButton).text = question.answers[3]

            // Временно отключаем слушатель чтобы избежать ложных срабатываний
            binding.radioGroup.setOnCheckedChangeListener(null)

            // Сбрасываем выбор
            binding.radioGroup.clearCheck()

            // Восстанавливаем сохраненный ответ если он есть
            if (userAnswers[position] != -1) {
                val radioButtonId = when (userAnswers[position]) {
                    0 -> R.id.radioButton1
                    1 -> R.id.radioButton2
                    2 -> R.id.radioButton3
                    3 -> R.id.radioButton4
                    else -> -1
                }
                if (radioButtonId != -1) {
                    binding.radioGroup.check(radioButtonId)
                }
            }

            // Устанавливаем слушатель для новых выборов
            binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                val answerIndex = when (checkedId) {
                    R.id.radioButton1 -> 0
                    R.id.radioButton2 -> 1
                    R.id.radioButton3 -> 2
                    R.id.radioButton4 -> 3
                    else -> -1
                }
                if (answerIndex != -1) {
                    onAnswerSelected(position, answerIndex)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val binding = QuestionItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return QuestionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        holder.bind(questions[position], position)
    }

    override fun getItemCount(): Int = questions.size
}