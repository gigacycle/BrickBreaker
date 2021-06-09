package gigacycle.projectilebrickbreaker.Model;

/**
 * Created by Gigacycle on 10/14/2018.
 */

public class Size {
    private int _width, _height;

    public Size()
    {
        _width = 0;
        _height = 0;
    }

    public Size(int width, int height)
    {
        _width = width;
        _height = height;
    }

    public int getWidth(){
        return _width;
    }

    public int getHeight() {
        return _height;
    }

    public void set(int width, int height)
    {
        _width = width;
        _height = height;
    }
}
