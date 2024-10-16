package com.ku.kodaero

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.ku.kodaero.databinding.FragmentKoyeonMenuBinding

class KoyeonMenuFragment : Fragment() {


    companion object {
        private const val ARG_PUB_DETAIL = "pub_detail"

        fun newInstance(pubDetail: PubDetail): KoyeonMenuFragment {
            val fragment = KoyeonMenuFragment()
            val args = Bundle()
            args.putParcelable(ARG_PUB_DETAIL, pubDetail)
            fragment.arguments = args
            return fragment
        }
    }

    private var _binding: FragmentKoyeonMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKoyeonMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = binding.name
        val address = binding.address
        val sponser = binding.sponser

        val pubDetail: PubDetail? = arguments?.getParcelable(ARG_PUB_DETAIL)

        name.text = pubDetail?.name
        address.text = pubDetail?.address ?: "주소 정보 없음"
        sponser.text = pubDetail?.sponsor

            if (pubDetail?.menus.isNullOrEmpty()) {
            // binding.emptyMessageTextView.visibility = View.VISIBLE
            // binding.emptyMessageTextView.text = "메뉴 정보가 없습니다."
        } else {
            val adapter = KoyeonMenuAdapter(pubDetail?.menus!!)
            binding.menuRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.menuRecyclerView.adapter = adapter
        }



        binding.modalDepartButton.setOnClickListener{
            pubDetail?.name?.let { it1 ->
                putBuildingDirectionsFragment(
                    isStartingPoint = true,
                    buildingName = it1, // 여기에 실제 건물 이름을 설정하세요
                    placeType = "COORD",
                    id = -1, // 여기에 실제 건물 ID를 설정하세요
                    lat = pubDetail.latitude,
                    lng = pubDetail.longitude
                )
            }
        }
        binding.modalArriveButton.setOnClickListener {
            pubDetail?.name?.let { it1 ->
                putBuildingDirectionsFragment(
                    isStartingPoint = false,
                    buildingName = it1, // 여기에 실제 건물 이름을 설정하세요
                    placeType = "COORD",
                    id = -1, // 여기에 실제 건물 ID를 설정하세요
                    lat = pubDetail.latitude,
                    lng = pubDetail.longitude
                )
            }
            if (pubDetail != null) {
                Log.e("fefqwfwqf","${pubDetail.latitude}")
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun putBuildingDirectionsFragment(isStartingPoint: Boolean, buildingName: String, placeType: String, id: Int, lat: Double, lng: Double) {
        setFragmentResult("requestKey", Bundle().apply {
            putString("buildingName", buildingName)
            putBoolean("isStartingPoint", isStartingPoint)
            putString("placeType", placeType)
            putInt("id", id)
            putDouble("lat", lat)
            putDouble("lng", lng)
        })

        val getDirectionsFragment = GetDirectionsFragment()
        val activity = context as? FragmentActivity
        activity?.supportFragmentManager?.beginTransaction()
            ?.add(R.id.main_container, getDirectionsFragment, "DirectionFragment")
            ?.addToBackStack("DirectionFragment")
            ?.commit()
    }




}
