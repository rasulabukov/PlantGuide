package com.example.plantguide

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.plantguide.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!
    private val WIKI_URL = "https://ru.wikipedia.org/wiki/Зерновые_культуры"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.websiteButton.setOnClickListener {
            openWikipediaPage()
        }
    }

    private fun openWikipediaPage() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(WIKI_URL))
            startActivity(intent)
        } catch (e: Exception) {
            // В случае ошибки (например, если нет браузера)
            Toast.makeText(
                requireContext(),
                "Не удалось открыть страницу",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}