from setuptools import setup
from Cython.Build import cythonize

setup(
    ext_modules=cythonize("ml.py", annotate=False, compiler_directives={'language_level' : "3"})
)