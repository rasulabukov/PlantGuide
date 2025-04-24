package com.example.plantguide

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.plantguide.databinding.FragmentHomeBinding
import com.example.plantguide.db.AppDatabase
import com.example.plantguide.db.Grains
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GrainsAdapter
    private lateinit var database: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = AppDatabase.getDatabase(requireContext())

        // Настройка RecyclerView
        adapter = GrainsAdapter(emptyList()) { grain ->
            showGrainDetailsDialog(grain)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2) // 2 колонки
            adapter = this@HomeFragment.adapter
        }

        // Загрузка данных
        loadGrainsData()
    }

    private fun showGrainDetailsDialog(grain: Grains) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.grain_detail_dialog)

        // Находим элементы в диалоге
        val image = dialog.findViewById<ImageView>(R.id.dialog_grain_image)
        val name = dialog.findViewById<TextView>(R.id.dialog_grain_name)
        val species = dialog.findViewById<TextView>(R.id.dialog_grain_species)
        val description = dialog.findViewById<TextView>(R.id.dialog_grain_description)
        val climate = dialog.findViewById<TextView>(R.id.dialog_grain_climate)
        val yield = dialog.findViewById<TextView>(R.id.dialog_grain_yield)
        val diseases = dialog.findViewById<TextView>(R.id.dialog_grain_diseases)
        val usage = dialog.findViewById<TextView>(R.id.dialog_grain_usage)
        val regions = dialog.findViewById<TextView>(R.id.dialog_grain_regions)
        val favButton = dialog.findViewById<Button>(R.id.dialog_fav_button)

        // Заполняем данные
        image.setImageResource(grain.imageResId)
        name.text = grain.name
        species.text = "${grain.species} (${grain.subspecies})"
        description.text = grain.fullDescription
        climate.text = "Климатические условия: ${grain.climateConditions}"
        yield.text = "Урожайность: ${grain.yield}"
        diseases.text = "Болезни и вредители: ${grain.diseases}"
        usage.text = "Использование: ${grain.usage}"
        regions.text = "Регионы произрастания: ${grain.growingRegions}"

        if (grain.isFavorite) {
            favButton.text = "В избранном"
            favButton.setTextColor(R.color.black)
            favButton.setBackgroundResource(R.drawable.bg_next_button_white)  // Устанавливаем стиль для избранного
            favButton.isEnabled = false  // Отключаем кнопку
        } else {
            favButton.text = "Добавить в избранное"
            favButton.setBackgroundResource(R.drawable.bg_next_button)  // Устанавливаем стиль для добавления
            favButton.isEnabled = true  // Включаем кнопку

            // Обработка нажатия кнопки добавления в избранное
            favButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    // Обновляем значение isFavourite в базе данных
                    grain.isFavorite = true  // Устанавливаем значение
                    database.grainsDao().update(grain)  // Обновляем запись

                    // Обновляем UI после закрытия диалога
                    activity?.runOnUiThread {
                        favButton.text = "В избранном"
                        favButton.setBackgroundResource(R.drawable.bg_next_button_white)  // Устанавливаем стиль для избранного
                        favButton.isEnabled = false  // Отключаем кнопку
                    }
                }
                dialog.dismiss()  // Закрываем диалог
            }
        }

        // Настройка диалога
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun loadGrainsData() {
        CoroutineScope(Dispatchers.IO).launch {
            // Проверяем, есть ли данные в БД
            val count = database.grainsDao().getAllGrains().firstOrNull()?.size ?: 0

            if (count == 0) {
                // Если БД пустая, добавляем тестовые данные
                insertSampleData()
            }

            // Получаем данные из БД
            database.grainsDao().getAllGrains().collect { grains ->
                activity?.runOnUiThread {
                    adapter.updateData(grains)
                }
            }
        }
    }

    private fun insertSampleData() {
        val grainsList = listOf(
            // Основные зерновые культуры
            Grains(
                name = "Пшеница",
                species = "Зерновые",
                subspecies = "Triticum",
                shortDescription = "Основная зерновая культура",
                fullDescription = "Подробное описание пшеницы...",
                climateConditions = "Умеренный климат",
                yield = "3-8 т/га",
                diseases = "Ржавчина, мучнистая роса",
                usage = "Хлеб, макароны, крупы",
                growingRegions = "Россия, США, Китай, Индия",
                imageResId = R.drawable.wheat
            ),
            Grains(
                name = "Рис",
                species = "Зерновые",
                subspecies = "Oryza sativa",
                shortDescription = "Основная культура Азии",
                fullDescription = "Подробное описание риса...",
                climateConditions = "Тропический и субтропический",
                yield = "4-10 т/га",
                diseases = "Пиракуляриоз, бактериальный ожог",
                usage = "Основной продукт питания",
                growingRegions = "Китай, Индия, Индонезия",
                imageResId = R.drawable.rice
            ),
            Grains(
                name = "Кукуруза",
                species = "Зерновые",
                subspecies = "Zea mays",
                shortDescription = "Важная кормовая культура",
                fullDescription = "Подробное описание кукурузы...",
                climateConditions = "Умеренный и тропический",
                yield = "5-12 т/га",
                diseases = "Пузырчатая головня",
                usage = "Корма, масло, крупы",
                growingRegions = "США, Китай, Бразилия",
                imageResId = R.drawable.corn
            ),
            Grains(
                name = "Ячмень",
                species = "Зерновые",
                subspecies = "Hordeum vulgare",
                shortDescription = "Для пивоварения",
                fullDescription = "Подробное описание ячменя...",
                climateConditions = "Умеренный климат",
                yield = "2-5 т/га",
                diseases = "Гельминтоспориоз",
                usage = "Пиво, крупы, корма",
                growingRegions = "Россия, Германия, Франция",
                imageResId = R.drawable.barley
            ),
            Grains(
                name = "Овёс",
                species = "Зерновые",
                subspecies = "Avena sativa",
                shortDescription = "Ценная пищевая культура",
                fullDescription = "Подробное описание овса...",
                climateConditions = "Умеренный климат",
                yield = "2-4 т/га",
                diseases = "Корончатая ржавчина",
                usage = "Овсяные хлопья, корма",
                growingRegions = "Россия, Канада, Польша",
                imageResId = R.drawable.oat
            ),
            Grains(
                name = "Рожь",
                species = "Зерновые",
                subspecies = "Secale cereale",
                shortDescription = "Морозоустойчивая культура",
                fullDescription = "Подробное описание ржи...",
                climateConditions = "Умеренный климат",
                yield = "2-4 т/га",
                diseases = "Стеблевая ржавчина",
                usage = "Ржаной хлеб, корма",
                growingRegions = "Россия, Германия, Польша",
                imageResId = R.drawable.rye
            ),
            Grains(
                name = "Сорго",
                species = "Зерновые",
                subspecies = "Sorghum bicolor",
                shortDescription = "Засухоустойчивая культура",
                fullDescription = "Подробное описание сорго...",
                climateConditions = "Тропический климат",
                yield = "2-5 т/га",
                diseases = "Головня, ржавчина",
                usage = "Крупы, корма, сироп",
                growingRegions = "Африка, Индия, США",
                imageResId = R.drawable.sorghum
            ),
            Grains(
                name = "Просо",
                species = "Зерновые",
                subspecies = "Panicum miliaceum",
                shortDescription = "Древняя зерновая культура",
                fullDescription = "Подробное описание проса...",
                climateConditions = "Умеренный климат",
                yield = "1-3 т/га",
                diseases = "Гельминтоспориоз",
                usage = "Пшённая крупа, корма",
                growingRegions = "Индия, Китай, Россия",
                imageResId = R.drawable.millet
            ),

            // Менее распространённые зерновые
            Grains(
                name = "Тритикале",
                species = "Зерновые (гибрид)",
                subspecies = "Triticosecale",
                shortDescription = "Гибрид пшеницы и ржи",
                fullDescription = "Подробное описание тритикале...",
                climateConditions = "Умеренный климат",
                yield = "3-6 т/га",
                diseases = "Ржавчина, фузариоз",
                usage = "Корм, хлебопечение",
                growingRegions = "Польша, Германия, Россия",
                imageResId = R.drawable.triticale
            ),
            Grains(
                name = "Чумиза",
                species = "Зерновые",
                subspecies = "Setaria italica",
                shortDescription = "Итальянское просо",
                fullDescription = "Подробное описание чумизы...",
                climateConditions = "Умеренный климат",
                yield = "1-3 т/га",
                diseases = "Головня, ржавчина",
                usage = "Крупы, корма",
                growingRegions = "Китай, Индия, Россия",
                imageResId = R.drawable.foxtail_millet
            ),
            Grains(
                name = "Гречиха",
                species = "Псевдозерновые",
                subspecies = "Fagopyrum esculentum",
                shortDescription = "Ценная медоносная культура",
                fullDescription = "Подробное описание гречихи...",
                climateConditions = "Умеренный климат",
                yield = "0.8-2 т/га",
                diseases = "Фитофтороз",
                usage = "Гречневая крупа, мёд",
                growingRegions = "Россия, Китай, Украина",
                imageResId = R.drawable.buckwheat
            ),
            Grains(
                name = "Амарант",
                species = "Псевдозерновые",
                subspecies = "Amaranthus",
                shortDescription = "Древняя культура ацтеков",
                fullDescription = "Подробное описание амаранта...",
                climateConditions = "Тёплый умеренный",
                yield = "1-3 т/га",
                diseases = "Пятнистости листьев",
                usage = "Крупы, мука, масло",
                growingRegions = "Перу, Мексика, Индия",
                imageResId = R.drawable.amaranth
            ),
            Grains(
                name = "Киноа",
                species = "Псевдозерновые",
                subspecies = "Chenopodium quinoa",
                shortDescription = "Золотое зерно инков",
                fullDescription = "Подробное описание киноа...",
                climateConditions = "Горные районы",
                yield = "0.5-3 т/га",
                diseases = "Мучнистая роса",
                usage = "Крупы, мука",
                growingRegions = "Перу, Боливия, Эквадор",
                imageResId = R.drawable.quinoa
            ),
            Grains(
                name = "Фонио",
                species = "Зерновые",
                subspecies = "Digitaria exilis",
                shortDescription = "Африканское просо",
                fullDescription = "Подробное описание фонио...",
                climateConditions = "Тропический климат",
                yield = "0.5-1.5 т/га",
                diseases = "Ржавчина",
                usage = "Крупы, пиво",
                growingRegions = "Западная Африка",
                imageResId = R.drawable.fonio
            ),
            Grains(
                name = "Тефф",
                species = "Зерновые",
                subspecies = "Eragrostis tef",
                shortDescription = "Мелкозернистая культура",
                fullDescription = "Подробное описание теффа...",
                climateConditions = "Тропический климат",
                yield = "0.5-1.5 т/га",
                diseases = "Ржавчина",
                usage = "Мука, каши",
                growingRegions = "Эфиопия, Эритрея",
                imageResId = R.drawable.teff
            ),

            // Основные зернобобовые
            Grains(
                name = "Горох",
                species = "Зернобобовые",
                subspecies = "Pisum sativum",
                shortDescription = "Популярная бобовая культура",
                fullDescription = "Подробное описание гороха...",
                climateConditions = "Умеренный климат",
                yield = "2-4 т/га",
                diseases = "Аскохитоз, фузариоз",
                usage = "Пища, корма, консервы",
                growingRegions = "Россия, Канада, Франция",
                imageResId = R.drawable.pea
            ),
            Grains(
                name = "Фасоль",
                species = "Зернобобовые",
                subspecies = "Phaseolus vulgaris",
                shortDescription = "Разнообразные виды",
                fullDescription = "Подробное описание фасоли...",
                climateConditions = "Тёплый умеренный",
                yield = "1-3 т/га",
                diseases = "Антракноз, бактериоз",
                usage = "Пища, консервы",
                growingRegions = "Бразилия, Индия, Китай",
                imageResId = R.drawable.beans
            ),
            Grains(
                name = "Чечевица",
                species = "Зернобобовые",
                subspecies = "Lens culinaris",
                shortDescription = "Древняя бобовая культура",
                fullDescription = "Подробное описание чечевицы...",
                climateConditions = "Умеренный климат",
                yield = "0.8-2 т/га",
                diseases = "Аскохитоз, фузариоз",
                usage = "Пища, диетическое питание",
                growingRegions = "Канада, Индия, Турция",
                imageResId = R.drawable.lentil
            ),
            Grains(
                name = "Нут",
                species = "Зернобобовые",
                subspecies = "Cicer arietinum",
                shortDescription = "Турецкий горох",
                fullDescription = "Подробное описание нута...",
                climateConditions = "Тёплый умеренный",
                yield = "0.8-2.5 т/га",
                diseases = "Фузариоз, аскохитоз",
                usage = "Хумус, фалафель",
                growingRegions = "Индия, Пакистан, Турция",
                imageResId = R.drawable.chickpea
            ),
            Grains(
                name = "Соя",
                species = "Зернобобовые",
                subspecies = "Glycine max",
                shortDescription = "Важнейшая белковая культура",
                fullDescription = "Подробное описание сои...",
                climateConditions = "Умеренный климат",
                yield = "2-4 т/га",
                diseases = "Фузариоз, ржавчина",
                usage = "Масло, корма, тофу",
                growingRegions = "США, Бразилия, Аргентина",
                imageResId = R.drawable.soybean
            ),
            Grains(
                name = "Бобы",
                species = "Зернобобовые",
                subspecies = "Vicia faba",
                shortDescription = "Конские бобы",
                fullDescription = "Подробное описание бобов...",
                climateConditions = "Умеренный климат",
                yield = "2-4 т/га",
                diseases = "Аскохитоз, ржавчина",
                usage = "Пища, корма",
                growingRegions = "Китай, Эфиопия, Австралия",
                imageResId = R.drawable.broad_beans
            ),
            Grains(
                name = "Люпин",
                species = "Зернобобовые",
                subspecies = "Lupinus",
                shortDescription = "Высокобелковая культура",
                fullDescription = "Подробное описание люпина...",
                climateConditions = "Умеренный климат",
                yield = "2-4 т/га",
                diseases = "Антракноз, фузариоз",
                usage = "Корма, пищевые добавки",
                growingRegions = "Австралия, Европа, Россия",
                imageResId = R.drawable.lupin
            ),
            Grains(
                name = "Вика",
                species = "Зернобобовые",
                subspecies = "Vicia sativa",
                shortDescription = "Мышиный горох",
                fullDescription = "Подробное описание вики...",
                climateConditions = "Умеренный климат",
                yield = "1.5-3 т/га",
                diseases = "Аскохитоз, ржавчина",
                usage = "Корма, сидерат",
                growingRegions = "Россия, Китай, США",
                imageResId = R.drawable.vetch
            ),

            // Менее распространённые зернобобовые
            Grains(
                name = "Маш",
                species = "Зернобобовые",
                subspecies = "Vigna radiata",
                shortDescription = "Зелёная фасоль",
                fullDescription = "Подробное описание маша...",
                climateConditions = "Тропический климат",
                yield = "0.5-1.5 т/га",
                diseases = "Мучнистая роса",
                usage = "Проростки, супы",
                growingRegions = "Индия, Китай, ЮВА",
                imageResId = R.drawable.mash
            ),
            Grains(
                name = "Адзуки",
                species = "Зернобобовые",
                subspecies = "Vigna angularis",
                shortDescription = "Красная фасоль",
                fullDescription = "Подробное описание адзуки...",
                climateConditions = "Умеренный климат",
                yield = "0.8-2 т/га",
                diseases = "Бактериоз, ржавчина",
                usage = "Десерты, супы",
                growingRegions = "Япония, Китай, Корея",
                imageResId = R.drawable.adzuki_bean
            ),
            Grains(
                name = "Чина",
                species = "Зернобобовые",
                subspecies = "Lathyrus sativus",
                shortDescription = "Индийский горох",
                fullDescription = "Подробное описание чины...",
                climateConditions = "Умеренный климат",
                yield = "1-2 т/га",
                diseases = "Мучнистая роса",
                usage = "Корма, пищевые продукты",
                growingRegions = "Индия, Бангладеш, Эфиопия",
                imageResId = R.drawable.grass_pea
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            database.grainsDao().insert(grainsList)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}