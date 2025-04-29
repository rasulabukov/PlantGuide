package com.example.plantguide

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.plantguide.databinding.ActivityTestBinding
import com.example.plantguide.databinding.ResultDialogBinding
import java.util.*

class TestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestBinding
    private lateinit var viewPager: ViewPager2
    private lateinit var testAdapter: TestAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val userAnswers = MutableList(10) { -1 }

    override fun attachBaseContext(newBase: Context) {
        sharedPreferences = newBase.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val langCode = sharedPreferences.getString("app_language", "ru") ?: "ru"
        val locale = Locale(langCode)
        val config = Configuration()
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        val questions = if (isEnglish()) getEnglishQuestions() else getRussianQuestions()

        testAdapter = TestAdapter(questions, userAnswers) { questionIndex, answerIndex ->
            userAnswers[questionIndex] = answerIndex
        }

        viewPager = binding.viewPager
        viewPager.adapter = testAdapter
        viewPager.isUserInputEnabled = false

        setupNavigation(questions)

        binding.backbtn.setOnClickListener { finish() }
    }

    private fun isEnglish(): Boolean {
        val config = resources.configuration
        return config.locales[0].language == "en"
    }

    private fun getRussianQuestions(): List<Question> {
        return listOf(
            Question(
                "Какая зерновая культура является основной для производства хлеба в России?",
                listOf("Рис", "Пшеница", "Кукуруза", "Ячмень"),
                1 // Пшеница
            ),
            Question(
                "Какая из этих культур не является зерновой?",
                listOf("Овес", "Гречиха", "Рожь", "Подсолнечник"),
                3 // Подсолнечник
            ),
            Question(
                "Какая зерновая культура занимает первое место в мире по посевным площадям?",
                listOf("Рис", "Пшеница", "Ячмень", "Кукуруза"),
                1 // Пшеница
            ),
            Question(
                "Как называется процесс отделения зерна от колоса?",
                listOf("Веяние", "Молотьба", "Жатва", "Полив"),
                1 // Молотьба
            ),
            Question(
                "Какая зерновая культура является основной для производства пива?",
                listOf("Пшеница", "Рис", "Ячмень", "Овес"),
                2 // Ячмень
            ),
            Question(
                "Какой злак используется для приготовления пшенной каши?",
                listOf("Пшеница", "Просо", "Овес", "Ячмень"),
                1 // Просо
            ),
            Question(
                "Какая культура является основной для производства итальянской пасты?",
                listOf("Твердая пшеница (дурум)", "Рис", "Ячмень", "Рожь"),
                0 // Твердая пшеница (дурум)
            ),
            Question(
                "Как называется самая древняя зерновая культура, возделываемая человеком?",
                listOf("Пшеница", "Ячмень", "Рис", "Просо"),
                1 // Ячмень
            ),
            Question(
                "Какая зерновая культура наиболее засухоустойчива?",
                listOf("Рис", "Пшеница", "Сорго", "Овес"),
                2 // Сорго
            ),
            Question(
                "Какой злак используется для производства овсяных хлопьев?",
                listOf("Ячмень", "Пшеница", "Овес", "Рожь"),
                2 // Овес
            )
        )
    }

    private fun getEnglishQuestions(): List<Question> {
        return listOf(
            Question(
                "Which grain crop is the main one for bread production in Russia?",
                listOf("Rice", "Wheat", "Corn", "Barley"),
                1 // Wheat
            ),
            Question(
                "Which of these crops is not a cereal grain?",
                listOf("Oats", "Buckwheat", "Rye", "Sunflower"),
                3 // Sunflower
            ),
            Question(
                "Which grain crop ranks first in the world in terms of cultivated area?",
                listOf("Rice", "Wheat", "Barley", "Corn"),
                1 // Wheat
            ),
            Question(
                "What is the process of separating grain from the ear called?",
                listOf("Winnowing", "Threshing", "Harvesting", "Irrigation"),
                1 // Threshing
            ),
            Question(
                "Which grain crop is the main one for beer production?",
                listOf("Wheat", "Rice", "Barley", "Oats"),
                2 // Barley
            ),
            Question(
                "Which cereal is used to make millet porridge?",
                listOf("Wheat", "Millet", "Oats", "Barley"),
                1 // Millet
            ),
            Question(
                "Which crop is the main one for Italian pasta production?",
                listOf("Durum wheat", "Rice", "Barley", "Rye"),
                0 // Durum wheat
            ),
            Question(
                "What is the name of the most ancient grain crop cultivated by humans?",
                listOf("Wheat", "Barley", "Rice", "Millet"),
                1 // Barley
            ),
            Question(
                "Which grain crop is the most drought-resistant?",
                listOf("Rice", "Wheat", "Sorghum", "Oats"),
                2 // Sorghum
            ),
            Question(
                "Which cereal is used to make oatmeal?",
                listOf("Barley", "Wheat", "Oats", "Rye"),
                2 // Oats
            )
        )
    }

    private fun setupNavigation(questions: List<Question>) {
        val bottomNav = binding.bottomNavigation

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_prev -> {
                    if (viewPager.currentItem > 0) {
                        viewPager.currentItem = viewPager.currentItem - 1
                    }
                    true
                }
                R.id.nav_next -> {
                    if (viewPager.currentItem < questions.size - 1) {
                        viewPager.currentItem = viewPager.currentItem + 1
                    } else {
                        showResults(questions)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun showResults(questions: List<Question>) {
        var correctAnswers = 0
        questions.forEachIndexed { index, question ->
            if (userAnswers[index] == question.correctAnswer) {
                correctAnswers++
            }
        }

        val resultBinding = ResultDialogBinding.inflate(layoutInflater)
        resultBinding.tvResult.text = getString(R.string.correct_answers, correctAnswers, questions.size)

        val rating = when (correctAnswers) {
            in 0..3 -> getString(R.string.rating_bad)
            in 4..6 -> getString(R.string.rating_satisfactory)
            in 7..9 -> getString(R.string.rating_good)
            10 -> getString(R.string.rating_excellent)
            else -> ""
        }

        resultBinding.tvRating.text = getString(R.string.your_rating, rating)

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.test_results))
            .setView(resultBinding.root)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setCancelable(false)
            .show()
    }
}