package jejusoul.com.github.obd_pids_for_hkmc_evs;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import jejusoul.com.github.obd_pids_for_hkmc_evs.utils.CSVDataManager;
import jejusoul.com.github.obd_pids_for_hkmc_evs.data.model.PIDData;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final MutableLiveData<List<PIDData>> pidData = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final CSVDataManager csvManager;

    public MainViewModel(Application application) {
        super(application);
        csvManager = new CSVDataManager(application);
    }
} 