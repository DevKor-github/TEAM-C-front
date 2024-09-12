package com.devkor.kodaero

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.devkor.kodaero.databinding.FragmentSuggestionBinding
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.talk.TalkApiClient

class SuggestionFragment : Fragment() {

    private var _binding: FragmentSuggestionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FetchDataViewModel
    private var type: String = ""
    private var title: String = ""
    private var content: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSuggestionBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(this).get(FetchDataViewModel::class.java)

        binding.backToMypageButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().supportFragmentManager.popBackStack()
        }

        setupListeners()

        return binding.root
    }

    private fun setupListeners() {
        binding.suggestionKakaoButton.setOnClickListener {
            navigateToKakao()
        }

        binding.suggestionInstarButton.setOnClickListener {
            navigateToInstar()
        }

        binding.suggestionSelectType.setOnClickListener {
            toggleSuggestionTypeScroll()
        }

        val suggestionTypeItems = listOf(
            binding.suggestionLOCATIONERROR,
            binding.suggestionINCORRECTNAME,
            binding.suggestionINCORRECTOPERATIONHOURS,
            binding.suggestionCLOSEDPLACE,
            binding.suggestionINCORRECTROUTE,
            binding.suggestionADDITIONALINFORMATION,
            binding.suggestionFUNCTIONALERROR,
            binding.suggestionFEATURESUGGESTION,
            binding.suggestionINCONVENIENCE,
            binding.suggestionQUESTION,
            binding.suggestionOTHER
        )

        suggestionTypeItems.forEach { textView ->
            textView.setOnClickListener { clickedView ->
                val selectedText = textView.text.toString()
                binding.suggestionSelectTypeText.text = selectedText
                binding.suggestionSelectTypeArrow.setImageResource(R.drawable.button_show_operating_time)
                binding.suggestionSelectTypeScroll.visibility = View.GONE
                type = resources.getResourceEntryName(textView.id).removePrefix("suggestion_")
            }
        }

        binding.suggestionTitle.addTextChangedListener {
            title = it.toString()
        }

        binding.suggestionContent.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                binding.suggestionContentCount.text = charCount.toString()

                if (charCount > 500) {
                    binding.suggestionContentCount.setTextColor(resources.getColor(R.color.red))
                    binding.suggestionContent.setText(s?.substring(0, 500))
                } else {
                    binding.suggestionContentCount.setTextColor(resources.getColor(R.color.map_sub))
                    content = s.toString()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.suggestionSummitButton.setOnClickListener {
            if (title.isNotBlank() && type.isNotBlank() && content.isNotBlank()) {
                viewModel.submitSuggestion(title, type, content)
                binding.suggestionSummitBlackLayout.visibility = View.VISIBLE
                binding.suggestionSummitLayout.visibility = View.VISIBLE
            } else {
                Toast.makeText(requireContext(), "모든 내용을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.suggestionSummitExitButton.setOnClickListener {
            hideSuggestionSummitConfirmation()
        }

        binding.goToHomeButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.main_container, HomeFragment())
                .commit()
        }
    }

    private fun navigateToKakao() {
        val url = TalkApiClient.instance.addChannelUrl("kodaero_ku")
        context?.let { KakaoCustomTabsClient.openWithDefault(it, url) }
    }

    private fun navigateToInstar() {
        var intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/kodaero_ku/"))
        startActivity(intent)
    }

    private fun toggleSuggestionTypeScroll() {
        if (binding.suggestionSelectTypeScroll.visibility == View.GONE) {
            binding.suggestionSelectTypeArrow.setImageResource(R.drawable.button_hide_operating_time)
            binding.suggestionSelectTypeScroll.visibility = View.VISIBLE
        } else {
            binding.suggestionSelectTypeArrow.setImageResource(R.drawable.button_show_operating_time)
            binding.suggestionSelectTypeScroll.visibility = View.GONE
        }
    }

    fun isSuggestionSummitConfirmationVisible(): Boolean {
        return binding.suggestionSummitLayout.visibility == View.VISIBLE && binding.suggestionSummitBlackLayout.visibility == View.VISIBLE
    }

    fun hideSuggestionSummitConfirmation() {
        binding.suggestionTitle.text.clear()
        binding.suggestionContent.text.clear()
        binding.suggestionSelectTypeText.text = "건의사항 종류 선택"
        binding.suggestionContentCount.text = "0"
        binding.suggestionContentCount.setTextColor(resources.getColor(R.color.map_sub))
        type = ""
        title = ""
        content = ""

        binding.suggestionSummitLayout.visibility = View.GONE
        binding.suggestionSummitBlackLayout.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
