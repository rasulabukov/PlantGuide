package com.example.plantguide

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
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
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: GrainsAdapter
    private lateinit var database: AppDatabase
    private lateinit var sharedPreferences: SharedPreferences

    override fun onAttach(context: Context) {
        super.onAttach(context)
        sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
    }

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

        val sharedPreferences = requireContext().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        adapter = GrainsAdapter(emptyList(), { grain ->
            showGrainDetailsDialog(grain)
        }, sharedPreferences)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = this@HomeFragment.adapter
        }

        loadGrainsData()
    }

    private fun showGrainDetailsDialog(grain: Grains) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.grain_detail_dialog)

        val isEnglish = isEnglish()

        // Инициализация элементов диалога
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
        val moreButton = dialog.findViewById<Button>(R.id.dialog_more_button)

        // Заполнение данных с учетом языка
        image.setImageResource(grain.imageResId)
        name.text = if (isEnglish) grain.englishName else grain.name
        species.text = if (isEnglish) grain.englishSpecies else grain.species
        description.text = if (isEnglish) grain.englishFullDescription else grain.fullDescription

        climate.text = if (isEnglish)
            "Climate conditions: ${grain.englishClimateConditions}"
        else
            "Климатические условия: ${grain.climateConditions}"

        yield.text = if (isEnglish)
            "Yield: ${grain.englishYield}"
        else
            "Урожайность: ${grain.yield}"

        diseases.text = if (isEnglish)
            "Diseases: ${grain.englishDiseases}"
        else
            "Болезни и вредители: ${grain.diseases}"

        usage.text = if (isEnglish)
            "Usage: ${grain.englishUsage}"
        else
            "Использование: ${grain.usage}"

        regions.text = if (isEnglish)
            "Growing regions: ${grain.englishGrowingRegions}"
        else
            "Регионы произрастания: ${grain.growingRegions}"

        // Настройка кнопки избранного
        if (grain.isFavorite) {
            favButton.text = if (isEnglish) "In favorites" else "В избранном"
            favButton.setTextColor(resources.getColor(R.color.black))
            favButton.setBackgroundResource(R.drawable.bg_next_button_white)
            favButton.isEnabled = false
        } else {
            favButton.text = if (isEnglish) "Add to favorites" else "Добавить в избранное"
            favButton.setBackgroundResource(R.drawable.bg_next_button)
            favButton.isEnabled = true

            favButton.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    grain.isFavorite = true
                    database.grainsDao().update(grain)

                    activity?.runOnUiThread {
                        favButton.text = if (isEnglish) "In favorites" else "В избранном"
                        favButton.setBackgroundResource(R.drawable.bg_next_button_white)
                        favButton.isEnabled = false
                    }
                }
            }
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()

        moreButton.setOnClickListener {
            val query = grain.name.replace(" ", "_") // Заменяем пробелы на подчеркивания для URL
            val url = "https://ru.wikipedia.org/wiki/$query" // Формируем URL для Wikipedia
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)) // Создаем Intent
            startActivity(intent) // Открываем браузер с URL
        }
    }

    private fun loadGrainsData() {
        CoroutineScope(Dispatchers.IO).launch {
            val count = database.grainsDao().getAllGrains().firstOrNull()?.size ?: 0

            if (count == 0) {
                insertSampleData()
            }

            database.grainsDao().getAllGrains().collect { grains ->
                activity?.runOnUiThread {
                    adapter.updateData(grains)
                }
            }
        }
    }

    private fun isEnglish(): Boolean {
        val lang = sharedPreferences.getString("app_language", "ru") ?: "ru"
        return lang == "en"
    }

    private fun insertSampleData() {
        val grainsList = listOf(
            // 1. Основные зерновые культуры (12 main grains)
            Grains(
                name = "Пшеница",
                englishName = "Wheat",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Triticum",
                shortDescription = "Основная зерновая культура",
                englishShortDescription = "Main cereal crop",
                fullDescription = "Пшеница - важнейшая зерновая культура, используемая для производства муки, хлеба, макаронных изделий.",
                englishFullDescription = "Wheat is the most important cereal crop used for flour, bread, pasta production.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "3-8 т/га",
                englishYield = "3-8 t/ha",
                diseases = "Ржавчина, мучнистая роса, фузариоз",
                englishDiseases = "Rust, powdery mildew, fusarium",
                usage = "Хлеб, макароны, крупы, корма",
                englishUsage = "Bread, pasta, cereals, fodder",
                growingRegions = "Россия, США, Китай, Индия, ЕС",
                englishGrowingRegions = "Russia, USA, China, India, EU",
                imageResId = R.drawable.wheat
            ),

            Grains(
                name = "Рис",
                englishName = "Rice",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Oryza sativa",
                shortDescription = "Основная культура Азии",
                englishShortDescription = "Main Asian crop",
                fullDescription = "Рис - основной продукт питания для более чем половины населения мира.",
                englishFullDescription = "Rice is the staple food for more than half of the world's population.",
                climateConditions = "Тропический и субтропический климат",
                englishClimateConditions = "Tropical and subtropical climate",
                yield = "4-10 т/га",
                englishYield = "4-10 t/ha",
                diseases = "Пиракуляриоз, бактериальный ожог, вирусные заболевания",
                englishDiseases = "Blast disease, bacterial blight, viral diseases",
                usage = "Основной продукт питания, крупы, крахмал",
                englishUsage = "Staple food, cereals, starch",
                growingRegions = "Китай, Индия, Индонезия, Бангладеш, Вьетнам",
                englishGrowingRegions = "China, India, Indonesia, Bangladesh, Vietnam",
                imageResId = R.drawable.rice
            ),

            Grains(
                name = "Кукуруза",
                englishName = "Corn",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Zea mays",
                shortDescription = "Важная кормовая культура",
                englishShortDescription = "Important fodder crop",
                fullDescription = "Кукуруза используется для производства кормов, масла, крупы и биоэтанола.",
                englishFullDescription = "Corn is used to produce fodder, oil, grits and bioethanol.",
                climateConditions = "Теплый умеренный и тропический климат",
                englishClimateConditions = "Warm temperate and tropical climate",
                yield = "5-12 т/га",
                englishYield = "5-12 t/ha",
                diseases = "Пузырчатая головня, ржавчина, фузариоз",
                englishDiseases = "Smut, rust, fusarium",
                usage = "Корма, масло, крупы, биоэтанол",
                englishUsage = "Fodder, oil, cereals, bioethanol",
                growingRegions = "США, Китай, Бразилия, Аргентина, Украина",
                englishGrowingRegions = "USA, China, Brazil, Argentina, Ukraine",
                imageResId = R.drawable.corn
            ),

            Grains(
                name = "Ячмень",
                englishName = "Barley",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Hordeum vulgare",
                shortDescription = "Для пивоварения и кормов",
                englishShortDescription = "For brewing and fodder",
                fullDescription = "Ячмень - древняя зерновая культура, используется в пивоварении, производстве круп и кормов.",
                englishFullDescription = "Barley is an ancient cereal crop used in brewing, cereal production and fodder.",
                climateConditions = "Умеренный климат, засухоустойчив",
                englishClimateConditions = "Temperate climate, drought-resistant",
                yield = "2-5 т/га",
                englishYield = "2-5 t/ha",
                diseases = "Гельминтоспориоз, мучнистая роса, ржавчина",
                englishDiseases = "Helminthosporiosis, powdery mildew, rust",
                usage = "Пиво, крупы, корма, солод",
                englishUsage = "Beer, cereals, fodder, malt",
                growingRegions = "Россия, Германия, Франция, Канада, Австралия",
                englishGrowingRegions = "Russia, Germany, France, Canada, Australia",
                imageResId = R.drawable.barley
            ),

            Grains(
                name = "Овёс",
                englishName = "Oats",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Avena sativa",
                shortDescription = "Ценная пищевая культура",
                englishShortDescription = "Valuable food crop",
                fullDescription = "Овёс используется для производства овсяных хлопьев, муки и кормов для животных.",
                englishFullDescription = "Oats are used to produce oatmeal, flour and animal feed.",
                climateConditions = "Умеренный климат, холодоустойчив",
                englishClimateConditions = "Temperate climate, cold-resistant",
                yield = "2-4 т/га",
                englishYield = "2-4 t/ha",
                diseases = "Корончатая ржавчина, стеблевая ржавчина",
                englishDiseases = "Crown rust, stem rust",
                usage = "Овсяные хлопья, мука, корма",
                englishUsage = "Oatmeal, flour, fodder",
                growingRegions = "Россия, Канада, Польша, Финляндия, Австралия",
                englishGrowingRegions = "Russia, Canada, Poland, Finland, Australia",
                imageResId = R.drawable.oat
            ),

            Grains(
                name = "Рожь",
                englishName = "Rye",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Secale cereale",
                shortDescription = "Морозоустойчивая культура",
                englishShortDescription = "Frost-resistant crop",
                fullDescription = "Рожь используется для производства ржаного хлеба, кормов и в качестве сидерата.",
                englishFullDescription = "Rye is used to produce rye bread, fodder and as green manure.",
                climateConditions = "Холодный умеренный климат",
                englishClimateConditions = "Cold temperate climate",
                yield = "2-4 т/га",
                englishYield = "2-4 t/ha",
                diseases = "Стеблевая ржавчина, снежная плесень",
                englishDiseases = "Stem rust, snow mold",
                usage = "Ржаной хлеб, корма, сидераты",
                englishUsage = "Rye bread, fodder, green manure",
                growingRegions = "Россия, Германия, Польша, Беларусь, Украина",
                englishGrowingRegions = "Russia, Germany, Poland, Belarus, Ukraine",
                imageResId = R.drawable.rye
            ),

            Grains(
                name = "Просо",
                englishName = "Millet",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Panicum miliaceum",
                shortDescription = "Древняя зерновая культура",
                englishShortDescription = "Ancient cereal crop",
                fullDescription = "Просо используется для производства пшённой крупы, кормов и в пищевой промышленности.",
                englishFullDescription = "Millet is used to produce millet groats, fodder and in food industry.",
                climateConditions = "Умеренный и субтропический климат",
                englishClimateConditions = "Temperate and subtropical climate",
                yield = "1-3 т/га",
                englishYield = "1-3 t/ha",
                diseases = "Гельминтоспориоз, головня",
                englishDiseases = "Helminthosporiosis, smut",
                usage = "Пшённая крупа, корма, пищевые продукты",
                englishUsage = "Millet groats, fodder, food products",
                growingRegions = "Индия, Китай, Россия, Нигерия, Нигер",
                englishGrowingRegions = "India, China, Russia, Nigeria, Niger",
                imageResId = R.drawable.millet
            ),

            Grains(
                name = "Сорго",
                englishName = "Sorghum",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Sorghum bicolor",
                shortDescription = "Засухоустойчивая культура",
                englishShortDescription = "Drought-resistant crop",
                fullDescription = "Сорго используется для производства крупы, кормов, сиропа и биоэтанола.",
                englishFullDescription = "Sorghum is used to produce groats, fodder, syrup and bioethanol.",
                climateConditions = "Тропический и субтропический климат",
                englishClimateConditions = "Tropical and subtropical climate",
                yield = "2-5 т/га",
                englishYield = "2-5 t/ha",
                diseases = "Головня, ржавчина, антракноз",
                englishDiseases = "Smut, rust, anthracnose",
                usage = "Крупы, корма, сироп, биоэтанол",
                englishUsage = "Groats, fodder, syrup, bioethanol",
                growingRegions = "Африка, Индия, США, Мексика, Австралия",
                englishGrowingRegions = "Africa, India, USA, Mexico, Australia",
                imageResId = R.drawable.sorghum
            ),

            Grains(
                name = "Тритикале",
                englishName = "Triticale",
                species = "Зерновые (гибрид)",
                englishSpecies = "Cereals (hybrid)",
                subspecies = "Triticosecale",
                shortDescription = "Гибрид пшеницы и ржи",
                englishShortDescription = "Wheat-rye hybrid",
                fullDescription = "Тритикале сочетает качества пшеницы и ржи, используется для кормов и пищевых продуктов.",
                englishFullDescription = "Triticale combines qualities of wheat and rye, used for fodder and food products.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "3-6 т/га",
                englishYield = "3-6 t/ha",
                diseases = "Ржавчина, фузариоз, септориоз",
                englishDiseases = "Rust, fusarium, septoria",
                usage = "Корма, хлебопечение, пищевые продукты",
                englishUsage = "Fodder, baking, food products",
                growingRegions = "Польша, Германия, Россия, Франция, Беларусь",
                englishGrowingRegions = "Poland, Germany, Russia, France, Belarus",
                imageResId = R.drawable.triticale
            ),

            Grains(
                name = "Гречиха",
                englishName = "Buckwheat",
                species = "Псевдозерновые",
                englishSpecies = "Pseudocereals",
                subspecies = "Fagopyrum esculentum",
                shortDescription = "Ценная медоносная культура",
                englishShortDescription = "Valuable honey plant",
                fullDescription = "Гречиха дает полезную крупу и является важным медоносом.",
                englishFullDescription = "Buckwheat provides healthy groats and is an important honey plant.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "0.8-2 т/га",
                englishYield = "0.8-2 t/ha",
                diseases = "Фитофтороз, серая гниль",
                englishDiseases = "Phytophthora, gray mold",
                usage = "Гречневая крупа, мёд, пищевые продукты",
                englishUsage = "Buckwheat groats, honey, food products",
                growingRegions = "Россия, Китай, Украина, Франция, Казахстан",
                englishGrowingRegions = "Russia, China, Ukraine, France, Kazakhstan",
                imageResId = R.drawable.buckwheat
            ),

            Grains(
                name = "Киноа",
                englishName = "Quinoa",
                species = "Псевдозерновые",
                englishSpecies = "Pseudocereals",
                subspecies = "Chenopodium quinoa",
                shortDescription = "Золотое зерно инков",
                englishShortDescription = "Golden grain of the Incas",
                fullDescription = "Киноа - высокобелковая культура, богатая аминокислотами и минералами.",
                englishFullDescription = "Quinoa is a high-protein crop rich in amino acids and minerals.",
                climateConditions = "Горные районы",
                englishClimateConditions = "Mountain regions",
                yield = "0.5-3 т/га",
                englishYield = "0.5-3 t/ha",
                diseases = "Мучнистая роса, бактериозы",
                englishDiseases = "Powdery mildew, bacteriosis",
                usage = "Крупы, мука, пищевые продукты",
                englishUsage = "Groats, flour, food products",
                growingRegions = "Перу, Боливия, Эквадор, США, Канада",
                englishGrowingRegions = "Peru, Bolivia, Ecuador, USA, Canada",
                imageResId = R.drawable.quinoa
            ),

            Grains(
                name = "Амарант",
                englishName = "Amaranth",
                species = "Псевдозерновые",
                englishSpecies = "Pseudocereals",
                subspecies = "Amaranthus",
                shortDescription = "Древняя культура ацтеков",
                englishShortDescription = "Ancient Aztec crop",
                fullDescription = "Амарант содержит высококачественный белок и используется в пищевой промышленности.",
                englishFullDescription = "Amaranth contains high-quality protein and is used in food industry.",
                climateConditions = "Тёплый умеренный климат",
                englishClimateConditions = "Warm temperate climate",
                yield = "1-3 т/га",
                englishYield = "1-3 t/ha",
                diseases = "Пятнистости листьев, корневые гнили",
                englishDiseases = "Leaf spots, root rots",
                usage = "Крупы, мука, масло, пищевые добавки",
                englishUsage = "Groats, flour, oil, food additives",
                growingRegions = "Перу, Мексика, Индия, Китай, США",
                englishGrowingRegions = "Peru, Mexico, India, China, USA",
                imageResId = R.drawable.amaranth
            ),

            // 2. Дополнительные зерновые культуры (12 additional grains)
            Grains(
                name = "Тефф",
                englishName = "Teff",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Eragrostis tef",
                shortDescription = "Мелкозернистая культура",
                englishShortDescription = "Small-grained crop",
                fullDescription = "Тефф - важная зерновая культура Эфиопии, богатая железом и кальцием.",
                englishFullDescription = "Teff is an important Ethiopian cereal crop rich in iron and calcium.",
                climateConditions = "Тропический климат",
                englishClimateConditions = "Tropical climate",
                yield = "0.5-1.5 т/га",
                englishYield = "0.5-1.5 t/ha",
                diseases = "Ржавчина, головня",
                englishDiseases = "Rust, smut",
                usage = "Мука, каши, пищевые продукты",
                englishUsage = "Flour, porridge, food products",
                growingRegions = "Эфиопия, Эритрея, Индия, Австралия, США",
                englishGrowingRegions = "Ethiopia, Eritrea, India, Australia, USA",
                imageResId = R.drawable.teff
            ),

            Grains(
                name = "Фонио",
                englishName = "Fonio",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Digitaria exilis",
                shortDescription = "Африканское просо",
                englishShortDescription = "African millet",
                fullDescription = "Фонио - древняя засухоустойчивая культура Западной Африки.",
                englishFullDescription = "Fonio is an ancient drought-resistant crop of West Africa.",
                climateConditions = "Тропический климат",
                englishClimateConditions = "Tropical climate",
                yield = "0.5-1.5 т/га",
                englishYield = "0.5-1.5 t/ha",
                diseases = "Ржавчина, вирусные заболевания",
                englishDiseases = "Rust, viral diseases",
                usage = "Крупы, пиво, традиционные блюда",
                englishUsage = "Groats, beer, traditional dishes",
                growingRegions = "Западная Африка (Мали, Буркина-Фасо, Гвинея)",
                englishGrowingRegions = "West Africa (Mali, Burkina Faso, Guinea)",
                imageResId = R.drawable.fonio
            ),

            Grains(
                name = "Чумиза",
                englishName = "Foxtail millet",
                species = "Зерновые",
                englishSpecies = "Cereals",
                subspecies = "Setaria italica",
                shortDescription = "Итальянское просо",
                englishShortDescription = "Italian millet",
                fullDescription = "Чумиза - древняя зерновая культура, используемая в Азии и Европе.",
                englishFullDescription = "Foxtail millet is an ancient cereal crop used in Asia and Europe.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "1-3 т/га",
                englishYield = "1-3 t/ha",
                diseases = "Головня, ржавчина",
                englishDiseases = "Smut, rust",
                usage = "Крупы, корма, традиционные блюда",
                englishUsage = "Groats, fodder, traditional dishes",
                growingRegions = "Китай, Индия, Россия, Украина, Казахстан",
                englishGrowingRegions = "China, India, Russia, Ukraine, Kazakhstan",
                imageResId = R.drawable.foxtail_millet
            ),

            Grains(
                name = "Горох",
                englishName = "Pea",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Pisum sativum",
                shortDescription = "Популярная бобовая культура",
                englishShortDescription = "Popular legume crop",
                fullDescription = "Горох - важная белковая культура, используемая в пищу и как корм.",
                englishFullDescription = "Pea is an important protein crop used for food and fodder.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "2-4 т/га",
                englishYield = "2-4 t/ha",
                diseases = "Аскохитоз, фузариоз, мучнистая роса",
                englishDiseases = "Ascochyta blight, fusarium, powdery mildew",
                usage = "Пища, консервы, корма",
                englishUsage = "Food, canned food, fodder",
                growingRegions = "Россия, Канада, Франция, Китай, Индия",
                englishGrowingRegions = "Russia, Canada, France, China, India",
                imageResId = R.drawable.pea
            ),

            Grains(
                name = "Фасоль",
                englishName = "Beans",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Phaseolus vulgaris",
                shortDescription = "Разнообразные виды",
                englishShortDescription = "Various types",
                fullDescription = "Фасоль - важный источник растительного белка в рационе человека.",
                englishFullDescription = "Beans are an important source of plant protein in human diet.",
                climateConditions = "Тёплый умеренный климат",
                englishClimateConditions = "Warm temperate climate",
                yield = "1-3 т/га",
                englishYield = "1-3 t/ha",
                diseases = "Антракноз, бактериоз, ржавчина",
                englishDiseases = "Anthracnose, bacteriosis, rust",
                usage = "Пища, консервы, корма",
                englishUsage = "Food, canned food, fodder",
                growingRegions = "Бразилия, Индия, Китай, США, Мексика",
                englishGrowingRegions = "Brazil, India, China, USA, Mexico",
                imageResId = R.drawable.beans
            ),

            Grains(
                name = "Чечевица",
                englishName = "Lentil",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Lens culinaris",
                shortDescription = "Древняя бобовая культура",
                englishShortDescription = "Ancient legume crop",
                fullDescription = "Чечевица - ценный источник белка, богата железом и фолиевой кислотой.",
                englishFullDescription = "Lentil is a valuable protein source, rich in iron and folic acid.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "0.8-2 т/га",
                englishYield = "0.8-2 t/ha",
                diseases = "Аскохитоз, фузариоз, ржавчина",
                englishDiseases = "Ascochyta blight, fusarium, rust",
                usage = "Пища, диетическое питание",
                englishUsage = "Food, dietary nutrition",
                growingRegions = "Канада, Индия, Турция, США, Австралия",
                englishGrowingRegions = "Canada, India, Turkey, USA, Australia",
                imageResId = R.drawable.lentil
            ),

            Grains(
                name = "Нут",
                englishName = "Chickpea",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Cicer arietinum",
                shortDescription = "Турецкий горох",
                englishShortDescription = "Turkish pea",
                fullDescription = "Нут - важная культура в средиземноморской и ближневосточной кухне.",
                englishFullDescription = "Chickpea is an important crop in Mediterranean and Middle Eastern cuisine.",
                climateConditions = "Тёплый умеренный климат",
                englishClimateConditions = "Warm temperate climate",
                yield = "0.8-2.5 т/га",
                englishYield = "0.8-2.5 t/ha",
                diseases = "Фузариоз, аскохитоз, ржавчина",
                englishDiseases = "Fusarium, ascochyta blight, rust",
                usage = "Хумус, фалафель, супы",
                englishUsage = "Hummus, falafel, soups",
                growingRegions = "Индия, Пакистан, Турция, Австралия, Мексика",
                englishGrowingRegions = "India, Pakistan, Turkey, Australia, Mexico",
                imageResId = R.drawable.chickpea
            ),

            Grains(
                name = "Соя",
                englishName = "Soybean",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Glycine max",
                shortDescription = "Важнейшая белковая культура",
                englishShortDescription = "Most important protein crop",
                fullDescription = "Соя - основной источник растительного белка и масла.",
                englishFullDescription = "Soybean is the main source of plant protein and oil.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "2-4 т/га",
                englishYield = "2-4 t/ha",
                diseases = "Фузариоз, ржавчина, бактериоз",
                englishDiseases = "Fusarium, rust, bacteriosis",
                usage = "Масло, корма, тофу, соевое молоко",
                englishUsage = "Oil, fodder, tofu, soy milk",
                growingRegions = "США, Бразилия, Аргентина, Китай, Индия",
                englishGrowingRegions = "USA, Brazil, Argentina, China, India",
                imageResId = R.drawable.soybean
            ),

            Grains(
                name = "Люпин",
                englishName = "Lupin",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Lupinus",
                shortDescription = "Высокобелковая культура",
                englishShortDescription = "High-protein crop",
                fullDescription = "Люпин содержит до 50% белка и используется в пищу и как корм.",
                englishFullDescription = "Lupin contains up to 50% protein and is used for food and fodder.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "2-4 т/га",
                englishYield = "2-4 t/ha",
                diseases = "Антракноз, фузариоз, ржавчина",
                englishDiseases = "Anthracnose, fusarium, rust",
                usage = "Корма, пищевые добавки, мука",
                englishUsage = "Fodder, food additives, flour",
                growingRegions = "Австралия, Европа, Россия, Чили, ЮАР",
                englishGrowingRegions = "Australia, Europe, Russia, Chile, South Africa",
                imageResId = R.drawable.lupin
            ),

            Grains(
                name = "Маш",
                englishName = "Mung bean",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Vigna radiata",
                shortDescription = "Зелёная фасоль",
                englishShortDescription = "Green bean",
                fullDescription = "Маш - популярная культура в азиатской кухне, особенно для проростков.",
                englishFullDescription = "Mung bean is a popular crop in Asian cuisine, especially for sprouts.",
                climateConditions = "Тропический климат",
                englishClimateConditions = "Tropical climate",
                yield = "0.5-1.5 т/га",
                englishYield = "0.5-1.5 t/ha",
                diseases = "Мучнистая роса, бактериоз, вирусы",
                englishDiseases = "Powdery mildew, bacteriosis, viruses",
                usage = "Проростки, супы, десерты",
                englishUsage = "Sprouts, soups, desserts",
                growingRegions = "Индия, Китай, Юго-Восточная Азия",
                englishGrowingRegions = "India, China, Southeast Asia",
                imageResId = R.drawable.mash
            ),

            Grains(
                name = "Вика",
                englishName = "Vetch",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Vicia sativa",
                shortDescription = "Мышиный горох",
                englishShortDescription = "Common vetch",
                fullDescription = "Вика используется как кормовая культура и сидерат.",
                englishFullDescription = "Vetch is used as fodder crop and green manure.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "1.5-3 т/га",
                englishYield = "1.5-3 t/ha",
                diseases = "Аскохитоз, ржавчина, мучнистая роса",
                englishDiseases = "Ascochyta blight, rust, powdery mildew",
                usage = "Корма, сидерат",
                englishUsage = "Fodder, green manure",
                growingRegions = "Россия, Китай, США, Европа",
                englishGrowingRegions = "Russia, China, USA, Europe",
                imageResId = R.drawable.vetch
            ),

            Grains(
                name = "Чина",
                englishName = "Grass pea",
                species = "Зернобобовые",
                englishSpecies = "Pulses",
                subspecies = "Lathyrus sativus",
                shortDescription = "Индийский горох",
                englishShortDescription = "Indian pea",
                fullDescription = "Чина - засухоустойчивая культура, важная в засушливых регионах.",
                englishFullDescription = "Grass pea is a drought-resistant crop important in arid regions.",
                climateConditions = "Умеренный климат",
                englishClimateConditions = "Temperate climate",
                yield = "1-2 т/га",
                englishYield = "1-2 t/ha",
                diseases = "Мучнистая роса, ржавчина",
                englishDiseases = "Powdery mildew, rust",
                usage = "Корма, пищевые продукты",
                englishUsage = "Fodder, food products",
                growingRegions = "Индия, Бангладеш, Эфиопия",
                englishGrowingRegions = "India, Bangladesh, Ethiopia",
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