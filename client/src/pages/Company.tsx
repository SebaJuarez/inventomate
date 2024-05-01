import { useAuth0 } from "@auth0/auth0-react";
import { useUser } from "../hook/useUser";
import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import company_settings from '../assets/icons/white-settings.svg'
import search from '../assets/icons/search-.svg'
import { SideNavbar } from "../components/SideNavbar";
import { getCompany } from "../utils/Database.service";

export const Company = () => {

  const { currentUser, setUser } = useUser()

  const { isAuthenticated, getAccessTokenSilently } = useAuth0();

  const [company, setCompany] = useState<Company | null>(null)

  useEffect(() => {

    const getToken = async () => {
      const accessToken = await getAccessTokenSilently()
      setUser(accessToken)

      const userCompany = await getCompany(accessToken)
      setCompany(userCompany)
    }

    isAuthenticated && getToken()

  }, [isAuthenticated])

  return (
    <main className="-text--color-black flex">
      <section className="hidden relative lg:block w-64">
        <SideNavbar />
      </section>
      <section className="m-auto mt-4 w-11/12 lg:w-7/12 xl:w-7/12">
        <section className="m-auto col-span-3">
          <div className="flex items-center">
            <div className="w-20 h-20 overflow-hidden mr-4">
              <img src={currentUser?.empresa?.logo} className='h-full object-cover' />
            </div>
            <h1 className="text-xl font-bold">{currentUser?.empresa?.nombreEmpresa}</h1>
          </div>
          <div className="flex">
            <ul className="py-2">
              <li>
                <h2 className="font-bold -text--color-semidark-violet">Owner</h2>
                <div className="flex items-center space-x-2">
                  <img
                    src={currentUser?.usuario.picture}
                    alt={currentUser?.empresa?.owner.nickname}
                    className="w-8 h-8 rounded-full"
                  />
                  <section className="text-sm">
                    <p className="-text--color-violet-user-email font-bold">{currentUser?.empresa?.owner.nickname}</p>
                    <p className="-text--color-violet-user-email">{currentUser?.empresa?.owner.email}</p>
                  </section>
                </div>
              </li>
              <li className="w-full flex space-x-2">
                <h2 className="font-bold -text--color-semidark-violet">Location</h2>
                <p>Location</p>
              </li>
            </ul>
            <Link to='./company-settings'
              className="flex items-center p-2 font-bold text-sm -bg--color-semidark-violet -text--color-white justify-center rounded-xl max-w-md m-auto mr-0 mb-0"
            >
              <img
                src={company_settings}
                className="w-4 mr-2"
              />
              <p className="overflow-hidden whitespace-nowrap text-ellipsis">Company Settings</p>
            </Link>
          </div>
        </section>
        <section className="my-4">
          <h2 className="font-bold -text--color-semidark-violet py-2 text-lg">Branches</h2>
          <div className="grid grid-cols-2">
            <Link to='/company/register-branch' className="-bg--color-border-very-lightest-grey p-2 rounded-lg font-semibold -text--color-mate-dark-violet w-32 text-center">+ Add Branch</Link>
            <form className="-bg--color-border-very-lightest-grey p-2 rounded-lg  w-full flex max-w-sm m-auto mr-0">
              <input type="text" placeholder="Search" className="-bg--color-border-very-lightest-grey w-full " />
              <img src={search} className="w-4" />
            </form>
          </div>
          <ul className="my-4 grid w-full m-auto">
            <li className="grid grid-cols-5 border-b p-2 -bg--color-mate-dark-violet -text--color-white font-bold rounded-t-lg">
              <p className="">ID</p>
              <p className="col-span-2">Name</p>
              <p className="col-span-2">Location</p>
            </li>
            {company?.sucursales.length === 0 ?
              <li className="-bg--color-border-very-lightest-grey h-48 grid place-content-center">
                <p className="font-medium text-lg text-center p-4">
                  It looks like there are no branches in your company yet. :(
                </p>
                <Link to='/company/register-branch' className="-bg--color-semidark-violet p-2 rounded-lg font-semibold -text--color-white w-32 text-center m-auto hover:opacity-80">Add Branch</Link>
              </li>
              : (
                company?.sucursales.map((branch) => {
                  return (
                    <li className="grid grid-cols-5 hover:opacity-60">
                      <p><Link to={`/company/branch/${branch.idSucursal}`} className="block p-2">{branch.idSucursal}</Link></p>
                      <p className="col-span-2"><Link to={`/company/branch/${branch.idSucursal}`} className="block p-2">{branch.nombre}</Link></p>
                      <p className="col-span-2"><Link to={`/company/branch/${branch.idSucursal}`} className="block p-2">{branch.ubicacion}</Link></p>
                    </li>
                  )
                })
              )
            }
          </ul>
        </section>
      </section>
    </main>
  );
};
