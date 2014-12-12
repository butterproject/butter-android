package com.popcorn.tv.interactors;

import com.popcorn.tv.interfaces.main.MainInteractorInputInterface;
import com.popcorn.tv.interfaces.main.MainInteractorOutputInterface;

public class MainInteractor implements MainInteractorInputInterface
{
    //region Attributes
    MainInteractorOutputInterface presenter;
    //endregion

    //region Constructors
    public MainInteractor(MainInteractorOutputInterface presenter)
    {
        this.presenter = presenter;
    }
    //endregion
}
